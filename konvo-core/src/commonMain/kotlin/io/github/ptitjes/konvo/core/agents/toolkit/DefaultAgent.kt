package io.github.ptitjes.konvo.core.agents.toolkit

import ai.koog.agents.core.agent.*
import ai.koog.agents.core.agent.config.*
import ai.koog.agents.core.agent.entity.*
import ai.koog.agents.core.feature.*
import ai.koog.agents.core.feature.config.*
import ai.koog.agents.core.feature.pipeline.*
import ai.koog.agents.core.tools.*
import ai.koog.agents.features.eventHandler.feature.*
import ai.koog.agents.features.opentelemetry.feature.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.model.*
import ai.koog.prompt.llm.*
import ai.koog.prompt.message.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.settings.*
import io.github.ptitjes.konvo.core.tools.*
import io.opentelemetry.exporter.otlp.trace.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import kotlinx.serialization.json.*
import kotlin.coroutines.*
import kotlin.time.Clock
import kotlin.uuid.*
import ai.koog.prompt.message.Message as KoogMessage

internal class DefaultAgent(
    private val systemPrompt: Prompt,
    private val welcomeMessage: String? = null,
    private val model: LLModel,
    val maxAgentIterations: Int = 50,
    val promptExecutor: PromptExecutor,
    private val strategy: (InteractionDevice.Agent) -> AIAgentGraphStrategy<KoogMessage.User, List<KoogMessage.Assistant>>,
    private val mcpSessionFactory: ((coroutineContext: CoroutineContext) -> McpHostSession)? = null,
    private val mcpServerNames: Set<String> = emptySet(),
    private val developerSettings: DeveloperSettings = DeveloperSettings(),
    private val installFeatures: GraphAIAgent.FeatureContext.(InteractionDevice.Agent) -> Unit = {},
) : Agent {
    private var prompt: Prompt = systemPrompt

    private suspend fun buildAgent(
        tools: List<ToolCard>?,
        conversationView: InteractionDevice.Agent,
    ): AIAgent<KoogMessage.User, List<KoogMessage.Assistant>> {
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

                if (developerSettings.openTelemetry.enabled) {
                    install(OpenTelemetry) {
                        setServiceInfo("konvo", "1.0.0")

                        addSpanExporter(
                            OtlpGrpcSpanExporter.builder()
                                .setEndpoint(developerSettings.openTelemetry.endpoint)
                                .build()
                        )

                        setVerbose(developerSettings.openTelemetry.verbose)
                    }
                }

                handleEvents {
                    onToolValidationFailed { eventContext ->
                        conversationView.act(
                            ToolUsage.Notification(
                                call = ToolUsage.Call(
                                    id = eventContext.toolCallId ?: newUniqueId(),
                                    tool = eventContext.toolName,
                                    arguments = eventContext.toolArgs,
                                ),
                                result = ToolUsage.CallResult.ExecutionFailure(eventContext.message),
                            ),
                        )
                    }
                    onToolCallCompleted { eventContext ->
                        val result = eventContext.toolResult
                        val structuredContent = (result as? JsonObject)?.get("structuredContent")

                        conversationView.act(
                            ToolUsage.Notification(
                                call = ToolUsage.Call(
                                    id = eventContext.toolCallId ?: newUniqueId(),
                                    tool = eventContext.toolName,
                                    arguments = eventContext.toolArgs,
                                ),
                                result = ToolUsage.CallResult.Success(
                                    structuredContent ?: result,
                                ),
                            ),
                        )
                    }
                    onToolCallFailed { eventContext ->
                        conversationView.act(
                            ToolUsage.Notification(
                                call = ToolUsage.Call(
                                    id = eventContext.toolCallId ?: newUniqueId(),
                                    tool = eventContext.toolName,
                                    arguments = eventContext.toolArgs,
                                ),
                                result = ToolUsage.CallResult.ExecutionFailure(
                                    eventContext.message,
                                ),
                            ),
                        )
                    }
                }

                installFeatures(conversationView)
            },
        )
    }

    override suspend fun restoreSession(
        transcript: List<Action<*>>,
        conversation: InteractionDevice.Agent,
    ): Unit = coroutineScope {
        // TODO implement this properly: restore state and prompt

        val messages = transcript.mapNotNull { event ->
            @Suppress("UNCHECKED_CAST")
            when (val details = event.payload) {
                is Messaging.Message -> (event as Action<Messaging.Message>).toKoogMessage()
                else -> null
            }
        }

        prompt = prompt(systemPrompt) {
            messages(messages)
        }

        if (transcript.isEmpty()) {
            conversation.act(Presence.Joining)
        }

        launch {
            val conversationJustStarted = prompt.messages.size == 1
            if (conversationJustStarted) {
                welcomeMessage?.let { content ->
                    conversation.act(Messaging.Message(content = listOf(Messaging.Part.Text(content))))
                    prompt = prompt(prompt) {
                        message(
                            KoogMessage.Assistant(
                                content = content,
                                metaInfo = ResponseMetaInfo(timestamp = Clock.System.now().toDeprecatedInstant())
                            )
                        )
                    }
                }
            }

            conversation.act(
                AgentCapabilities.Messaging(
                    supportedMediaTypes = listOf(),
                )
            )

            mcpSessionFactory?.invoke(coroutineContext).use { mcpHostSession ->
                mcpHostSession?.addServers(mcpServerNames)
                val tools = mcpHostSession?.tools?.first()

                conversation.actions.buffer(Channel.UNLIMITED).collect { event ->
                    when (val details = event.payload) {
                        is Messaging.Message -> {
                            if (event.sender is Participant.User) {
                                conversation.act(AgentProcessing.Start)
                                val agent = buildAgent(tools, conversation)
                                @Suppress("UNCHECKED_CAST") val result =
                                    agent.run((event as Action<Messaging.Message>).toKoogMessage() as KoogMessage.User)
                                result.forEach {
                                    conversation.act(
                                        Messaging.Message(content = listOf(Messaging.Part.Text(it.content)))
                                    )
                                }
                                conversation.act(AgentProcessing.Completion)
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

private class PromptCollector {
    companion object Feature : AIAgentGraphFeature<PromptCollectorConfig, PromptCollector> {
        override val key: AIAgentStorageKey<PromptCollector> = AIAgentStorageKey("agents-features-prompt-collector")

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
