package io.github.ptitjes.konvo.core.agents.toolkit

import ai.koog.agents.core.agent.*
import ai.koog.agents.core.agent.config.*
import ai.koog.agents.core.agent.entity.*
import ai.koog.agents.core.feature.*
import ai.koog.agents.core.feature.config.*
import ai.koog.agents.core.feature.pipeline.*
import ai.koog.agents.core.tools.*
import ai.koog.agents.features.eventHandler.feature.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.model.*
import ai.koog.prompt.llm.*
import ai.koog.prompt.message.*
import com.eygraber.uri.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.Attachment
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.Part
import io.github.ptitjes.konvo.core.conversations.model.events.ToolUsage.Call
import io.github.ptitjes.konvo.core.conversations.model.events.ToolUsage.CallResult
import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.tools.*
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
import kotlin.uuid.*
import ai.koog.prompt.message.ContentPart as KoogContentPart
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.Message as EventMessage

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

    private suspend fun buildAgent(
        tools: List<ToolCard>?,
        conversationView: ConversationAgentView,
    ): AIAgent<Message.User, List<Message.Assistant>> {
        val tools = tools ?: emptyList()

        val agentConfig = AIAgentConfig(
            prompt = prompt,
            model = model,
            maxAgentIterations = maxAgentIterations,
        )

        val toolRegistry = tools.map { it.toTool() }.let { tools ->
            ToolRegistry {
                tools(tools)
            }
        }

        return AIAgent(
            promptExecutor = promptExecutor,
            strategy = strategy(conversationView),
            agentConfig = agentConfig,
            toolRegistry = toolRegistry,
            installFeatures = {
                install(PromptCollector) {
                    collectPrompt = { newPrompt ->
                        prompt = newPrompt
                    }
                }

                install(ConversationFeature) {
                    conversationViewProvider = { conversationView }
                    this.tools = tools
                }

                handleEvents {
                    onToolValidationFailed { eventContext ->
                        conversationView.sendToolUseResult(
                            call = Call(
                                id = eventContext.toolCallId ?: newUniqueId(),
                                tool = eventContext.tool.name,
                                arguments = eventContext.tool.encodeArgsUnsafe(eventContext.toolArgs)
                            ),
                            result = CallResult.ExecutionFailure(eventContext.error),
                        )
                    }
                    onToolCallCompleted { eventContext ->
                        conversationView.sendToolUseResult(
                            call = Call(
                                id = eventContext.toolCallId ?: newUniqueId(),
                                tool = eventContext.tool.name,
                                arguments = eventContext.tool.encodeArgsUnsafe(eventContext.toolArgs)
                            ),
                            result = CallResult.Success(eventContext.tool.encodeResultToStringUnsafe(eventContext.result)),
                        )
                    }
                    onToolCallFailed { eventContext ->
                        conversationView.sendToolUseResult(
                            call = Call(
                                id = eventContext.toolCallId ?: newUniqueId(),
                                tool = eventContext.tool.name,
                                arguments = eventContext.tool.encodeArgsUnsafe(eventContext.toolArgs)
                            ),
                            result = CallResult.ExecutionFailure(eventContext.throwable.message ?: "Tool failed"),
                        )
                    }
                }

                installFeatures(conversationView)
            },
        )
    }

    override suspend fun restorePrompt(events: List<Event>) {
        val messages = events.mapNotNull { event ->
            when (val details = event.payload) {
                is EventMessage -> event.toKoogMessage(details)
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
                conversation.sendMessage(listOf(Part.Text(content)))
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

        mcpSessionFactory?.invoke(coroutineContext).use { mcpHostSession ->
            mcpHostSession?.addServers(mcpServerNames)
            val tools = mcpHostSession?.tools?.first()

            conversation.events.buffer(Channel.UNLIMITED).collect { event ->
                when (val details = event.payload) {
                    is EventMessage -> {
                        if (event.sender is Participant.User) {
                            conversation.sendProcessing(true)
                            val agent = buildAgent(tools, conversation)
                            val result = agent.run(event.toKoogMessage(details) as Message.User)
                            result.forEach { conversation.sendMessage(listOf(Part.Text(it.content))) }
                            conversation.sendProcessing(false)
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun Event.toKoogMessage(details: EventMessage): Message =
        when (sender) {
            is Participant.User -> Message.User(
                parts = details.content.map { it.toKoogContentPart() },
                metaInfo = RequestMetaInfo(timestamp = timestamp.toDeprecatedInstant())
            )

            is Participant.Agent -> Message.Assistant(
                parts = details.content.map { it.toKoogContentPart() },
                metaInfo = ResponseMetaInfo(timestamp = timestamp.toDeprecatedInstant())
            )
        }

    private suspend fun Part.toKoogContentPart(): KoogContentPart = when (this) {
        is Part.Text -> KoogContentPart.Text(text)
        is Part.Image -> media.toKoogAttachment()
        is Part.Video -> media.toKoogAttachment()
        is Part.Audio -> media.toKoogAttachment()
        is Part.File -> media.toKoogAttachment()
        is Part.Embed<*> -> error("Embed content part is not supported for Koog")
    }

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

private fun newUniqueId(): String = Uuid.random().toString()
