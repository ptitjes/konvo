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
import kotlinx.serialization.json.*
import kotlin.coroutines.*
import kotlin.time.*
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
    override suspend fun restoreSession(
        transcript: ConversationTranscript,
        invite: Action<ConversationControl.InviteAgent>,
        device: InteractionDevice.Agent,
    ): AgentSession = coroutineScope {
        val context = DefaultAgentContext(
            coroutineContext = coroutineContext,
            mcpSessionFactory = mcpSessionFactory,
            initialPrompt = { systemPrompt },
        )

        val session = DefaultAgentSession(
            context = context,
        )

        val conversationJustStarted = transcript.actions.none { it.sender == device.participant }

        val presenceInteraction = if (conversationJustStarted) {
            val interaction = device.startInteraction(
                protocol = InteractionProtocol(
                    id = "$PLUGIN_ID/Agent#Presence",
                    awaitsInput = true,
                    hidesParent = false,
                    reactsTo = setOf(Messaging.Message::class),
                ),
                trigger = invite,
            )

            device.act(Presence.Joining, interaction)

            welcomeMessage?.let { content ->
                device.act(content.toKonvoMessage(), interaction)
                context.appendToPrompt {
                    message(
                        KoogMessage.Assistant(
                            content = content, metaInfo = ResponseMetaInfo(timestamp = Clock.System.now())
                        )
                    )
                }
            }

            interaction
        } else {
            val pendingInteractions = mutableMapOf<String, Interaction>()
            val messages = mutableListOf<KoogMessage>()

            transcript.entries.forEach { entry ->
                when (entry) {
                    is InteractionBoundary.Start -> {
                        val interaction = entry.interaction
                        pendingInteractions[interaction.id] = entry.interaction
                    }

                    is InteractionBoundary.End -> {
                        val interaction = entry.interaction
                        pendingInteractions.remove(interaction.id)
                    }

                    is Action<*> -> {
                        when (entry.payload) {
                            is Messaging.Message -> {
                                val action = entry.asTypedAction<Messaging.Message>()
                                messages += action.toKoogMessage()
                            }
                        }
                    }
                }
            }

            val interaction = pendingInteractions.values.last()

            check(interaction.protocol.id == "$PLUGIN_ID/Agent#Presence") {
                "Expected presence interaction, got ${interaction.protocol.id}"
            }

            context.appendToPrompt {
                messages(messages)
            }

            interaction
        }

        launch {
            device.act(AgentCapabilities.Messaging(supportedMediaTypes = listOf()), presenceInteraction)

            device.actions.buffer(Channel.UNLIMITED).collect { event ->
                when (val details = event.payload) {
                    is Messaging.Message -> {
                        if (event.sender is Participant.User) {
                            // Start a new interaction for processing this message
                            val action = event as Action<Messaging.Message>

                            device.withInteraction(
                                protocol = AgentProcessing.TurnBased,
                                parent = presenceInteraction,
                                trigger = action
                            ) {
                                device.act(AgentProcessing.Start, this)

                                @Suppress("UNCHECKED_CAST")
                                val agentInput = action.toKoogMessage() as KoogMessage.User

                                context.withMcpSession { mcpSession ->
                                    mcpSession.addServers(mcpServerNames)
                                    val tools = mcpSession.tools.first()

                                    val agent = buildAgent(context, tools, device)

                                    val agentOutput = agent.run(agentInput)

                                    agentOutput.forEach { assistantMessage ->
                                        device.act(assistantMessage.toKonvoMessage(), this)
                                    }
                                }

                                device.act(AgentProcessing.Completion, this)
                            }
                        }
                    }

                    else -> {}
                }
            }
        }

        session
    }

    private suspend fun buildAgent(
        context: DefaultAgentContext,
        tools: List<ToolCard>?,
        device: InteractionDevice.Agent,
    ): AIAgent<KoogMessage.User, List<KoogMessage.Assistant>> {
        val tools = tools ?: emptyList()

        val agentConfig = AIAgentConfig(
            prompt = context.prompt,
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
            strategy = strategy(device),
            agentConfig = agentConfig,
            toolRegistry = toolRegistry,
            installFeatures = {
                install(PromptCollector) {
                    collectPrompt = { newPrompt ->
                        context.resetPrompt(newPrompt)
                    }
                }

                install(ConversationFeature) {
                    conversationViewProvider = { device }
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
                        device.act(
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

                        device.act(
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
                        device.act(
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

                installFeatures(device)
            },
        )
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
