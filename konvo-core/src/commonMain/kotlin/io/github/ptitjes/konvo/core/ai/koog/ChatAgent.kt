package io.github.ptitjes.konvo.core.ai.koog

import ai.koog.agents.core.agent.*
import ai.koog.agents.core.agent.config.*
import ai.koog.agents.core.agent.entity.*
import ai.koog.agents.core.feature.*
import ai.koog.agents.core.tools.*
import ai.koog.agents.features.common.config.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.model.*
import ai.koog.prompt.llm.*
import ai.koog.prompt.message.*
import com.eygraber.uri.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.model.Attachment
import io.github.ptitjes.konvo.core.util.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import kotlinx.io.files.*
import kotlin.time.*
import kotlin.time.Clock
import ai.koog.prompt.message.Attachment as KoogAttachment

class ChatAgent(
    private val systemPrompt: Prompt,
    private val welcomeMessage: String? = null,
    private val model: LLModel,
    val maxAgentIterations: Int = 50,
    val promptExecutor: PromptExecutor,
    private val strategy: (ConversationAgentView) -> AIAgentStrategy<Message.User, List<Message.Assistant>>,
    private val toolRegistry: ToolRegistry = ToolRegistry.EMPTY,
    private val installFeatures: AIAgent.FeatureContext.(ConversationAgentView) -> Unit = {},
) : Agent {
    private var prompt: Prompt = systemPrompt

    private fun buildAgent(conversation: ConversationAgentView): AIAgent<Message.User, List<Message.Assistant>> {
        val agentConfig = AIAgentConfig(
            prompt = prompt,
            model = model,
            maxAgentIterations = maxAgentIterations,
        )

        return AIAgent(
            promptExecutor = promptExecutor,
            strategy = strategy(conversation),
            agentConfig = agentConfig,
            toolRegistry = toolRegistry,
            installFeatures = {
                install(PromptCollector) {
                    collectPrompt = { newPrompt ->
                        prompt = newPrompt
                    }
                }
                installFeatures(conversation)
            },
        )
    }

    override suspend fun restorePrompt(events: List<Event>) {
        val messages = events.mapNotNull { event ->
            when (event) {
                is Event.UserMessage -> event.toUserMessage()
                is Event.AssistantMessage -> event.toAssistantMessage()
                else -> null
            }
        }

        prompt = prompt(systemPrompt) {
            messages(messages)
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun joinConversation(conversation: ConversationAgentView) {
        if (conversation.transcript.events.isEmpty()) {
            welcomeMessage?.let { content ->
                conversation.sendMessage(content)
                prompt = prompt(prompt) {
                    message(
                        Message.Assistant(
                            content = content,
                            metaInfo = ResponseMetaInfo(timestamp = Clock.System.now().toDeprecatedInstant())
                        )
                    )
                }
            }
        }

        conversation.events.buffer(Channel.UNLIMITED).collect { event ->
            when (event) {
                is Event.UserMessage -> {
                    conversation.sendProcessing(true)
                    val agent = buildAgent(conversation)
                    val result = agent.run(event.toUserMessage())
                    result.forEach { conversation.sendMessage(it.content) }
                    conversation.sendProcessing(false)
                }

                else -> {}
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun Event.UserMessage.toUserMessage(): Message.User =
        Message.User(
            content = content,
            attachments = attachments.map { it.toKoogAttachment() },
            metaInfo = RequestMetaInfo(
                timestamp = timestamp.toDeprecatedInstant(),
            ),
        )

    @OptIn(ExperimentalTime::class)
    private fun Event.AssistantMessage.toAssistantMessage(): Message.Assistant =
        Message.Assistant(
            content = content,
            metaInfo = ResponseMetaInfo(
                timestamp = timestamp.toDeprecatedInstant(),
            ),
        )

    private val httpClient = HttpClient(CIO)

    private suspend fun Attachment.loadContent(): ByteArray {
        val uri = Uri.parse(url)

        return when {
            uri.scheme == "file" -> {
                val path = Path(uri.path ?: error("Invalid file path: $url"))
                SystemFileSystem.readBytes(path).toByteArray()
            }

            else -> httpClient.get(url).bodyAsBytes()
        }
    }

    private suspend fun Attachment.toKoogAttachment(): KoogAttachment {
        val bytes = loadContent()
        val content = AttachmentContent.Binary.Bytes(bytes)

        return when (type) {
            Attachment.Type.Audio -> KoogAttachment.Audio(
                content = content,
                format = name.substringAfterLast('.'),
                mimeType = mimeType,
                fileName = name,
            )

            Attachment.Type.Image -> KoogAttachment.Image(
                content = content,
                format = name.substringAfterLast('.'),
                mimeType = mimeType,
                fileName = name,
            )

            Attachment.Type.Video -> KoogAttachment.Video(
                content = content,
                format = name.substringAfterLast('.'),
                mimeType = mimeType,
                fileName = name,
            )

            Attachment.Type.Document -> KoogAttachment.File(
                content = content,
                format = name.substringAfterLast('.'),
                mimeType = mimeType,
                fileName = name,
            )
        }
    }
}

private class PromptCollector {
    companion object Feature : AIAgentFeature<PromptCollectorConfig, PromptCollector> {
        override val key: AIAgentStorageKey<PromptCollector> =
            AIAgentStorageKey("agents-features-prompt-collector")

        override fun createInitialConfig(): PromptCollectorConfig = PromptCollectorConfig()

        override fun install(config: PromptCollectorConfig, pipeline: AIAgentPipeline) {
            val featureImpl = PromptCollector()
            val interceptContext = InterceptContext(this, featureImpl)

            pipeline.interceptAfterNode(interceptContext) { eventContext ->
                config.collectPrompt(eventContext.context.llm.readSession { prompt })
            }
        }
    }
}

private class PromptCollectorConfig : FeatureConfig() {
    var collectPrompt: (Prompt) -> Unit = {}
}
