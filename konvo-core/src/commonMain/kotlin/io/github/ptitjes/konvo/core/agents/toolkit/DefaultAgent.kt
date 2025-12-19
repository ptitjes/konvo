package io.github.ptitjes.konvo.core.agents.toolkit

import ai.koog.agents.core.agent.*
import ai.koog.agents.core.agent.config.*
import ai.koog.agents.core.agent.entity.*
import ai.koog.agents.core.feature.*
import ai.koog.agents.core.feature.config.*
import ai.koog.agents.core.feature.pipeline.*
import ai.koog.agents.core.tools.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.model.*
import ai.koog.prompt.llm.*
import ai.koog.prompt.message.*
import com.eygraber.uri.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.util.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import kotlinx.io.files.*
import kotlin.coroutines.*
import kotlin.time.Clock
import ai.koog.prompt.message.ContentPart as KoogContentPart

internal class DefaultAgent(
    private val systemPrompt: Prompt,
    private val welcomeMessage: String? = null,
    private val model: LLModel,
    val maxAgentIterations: Int = 50,
    val promptExecutor: PromptExecutor,
    private val strategy: (ConversationAgentView) -> AIAgentGraphStrategy<Message.User, List<Message.Assistant>>,
    private val mcpSessionFactory: ((coroutineContext: CoroutineContext) -> McpHostSession)? = null,
    private val mcpServerNames: Set<String> = emptySet(),
    private val installFeatures: GraphAIAgent.FeatureContext.(ConversationAgentView) -> Unit = {},
) : Agent {
    private var prompt: Prompt = systemPrompt

    private fun buildAgent(
        toolRegistry: ToolRegistry,
        conversation: ConversationAgentView,
    ): AIAgent<Message.User, List<Message.Assistant>> {
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
            when (val details = event.payload) {
                is Event.UserMessage -> event.toUserMessage(details)
                is Event.AssistantMessage -> event.toAssistantMessage(details)
                else -> null
            }
        }

        prompt = prompt(systemPrompt) {
            messages(messages)
        }
    }

    override suspend fun joinConversation(conversation: ConversationAgentView) = coroutineScope {
        val conversationJustStarted = prompt.messages.size == 1
        if (conversationJustStarted) {
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

        val mcpHostSession = mcpSessionFactory?.invoke(coroutineContext)

        mcpHostSession?.addServers(mcpServerNames)
        val tools = mcpHostSession?.tools?.first()
        val toolRegistry = tools?.map { it.toTool() }.let { ToolRegistry { if (it != null) tools(it) } }

        try {
            conversation.events.buffer(Channel.UNLIMITED).collect { event ->
                when (val details = event.payload) {
                    is Event.UserMessage -> {
                        conversation.sendProcessing(true)
                        val agent = buildAgent(toolRegistry, conversation)
                        val result = agent.run(event.toUserMessage(details))
                        result.forEach { conversation.sendMessage(it.content) }
                        conversation.sendProcessing(false)
                    }

                    else -> {}
                }
            }
        } finally {
            mcpHostSession?.close()
        }
    }

    private suspend fun Event.toUserMessage(details: Event.UserMessage): Message.User =
        Message.User(
            parts = listOf(KoogContentPart.Text(details.content)) + details.attachments.map { it.toKoogAttachment() },
            metaInfo = RequestMetaInfo(
                timestamp = timestamp.toDeprecatedInstant(),
            ),
        )

    private fun Event.toAssistantMessage(details: Event.AssistantMessage): Message.Assistant =
        Message.Assistant(
            content = details.content,
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

    private suspend fun Attachment.toKoogAttachment(): KoogContentPart {
        val bytes = loadContent()
        val content = AttachmentContent.Binary.Bytes(bytes)

        return when (type) {
            Attachment.Type.Audio -> KoogContentPart.Audio(
                content = content,
                format = name.substringAfterLast('.'),
                mimeType = mimeType,
                fileName = name,
            )

            Attachment.Type.Image -> KoogContentPart.Image(
                content = content,
                format = name.substringAfterLast('.'),
                mimeType = mimeType,
                fileName = name,
            )

            Attachment.Type.Video -> KoogContentPart.Video(
                content = content,
                format = name.substringAfterLast('.'),
                mimeType = mimeType,
                fileName = name,
            )

            Attachment.Type.Document -> KoogContentPart.File(
                content = content,
                format = name.substringAfterLast('.'),
                mimeType = mimeType,
                fileName = name,
            )
        }
    }
}

private class PromptCollector {
    companion object Feature : AIAgentGraphFeature<PromptCollectorConfig, PromptCollector> {
        override val key: AIAgentStorageKey<PromptCollector> =
            AIAgentStorageKey("agents-features-prompt-collector")

        override fun createInitialConfig(): PromptCollectorConfig = PromptCollectorConfig()

        override fun install(config: PromptCollectorConfig, pipeline: AIAgentGraphPipeline): PromptCollector {
            val promptCollector = PromptCollector()

            pipeline.interceptNodeExecutionCompleted(this) { eventContext ->
                config.collectPrompt(eventContext.context.llm.readSession { prompt })
            }

            return promptCollector
        }
    }
}

private class PromptCollectorConfig : FeatureConfig() {
    var collectPrompt: (Prompt) -> Unit = {}
}
