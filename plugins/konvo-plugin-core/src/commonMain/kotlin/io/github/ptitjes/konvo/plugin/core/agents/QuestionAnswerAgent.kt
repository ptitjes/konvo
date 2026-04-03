package io.github.ptitjes.konvo.plugin.core.agents

import ai.koog.agents.core.agent.entity.*
import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.dsl.extension.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.llms.*
import ai.koog.prompt.markdown.*
import ai.koog.prompt.message.*
import io.github.ptitjes.konvo.plugin.core.agents.toolkit.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.events.*
import io.github.ptitjes.konvo.plugin.core.mcp.*
import io.github.ptitjes.konvo.plugin.core.models.*
import io.github.ptitjes.konvo.plugin.core.settings.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import kotlinx.datetime.format.*
import kotlin.coroutines.*
import kotlin.time.Clock

class QuestionAnswerAgent(
    private val settingsRepository: SettingsRepository,
    private val modelProviderManager: ModelManager,
    mcpSessionFactory: (coroutineContext: CoroutineContext) -> McpHostSession,
) : InteractiveAgent<QuestionAnswerAgentConfiguration>(
    configurationClass = QuestionAnswerAgentConfiguration::class,
    initialPrompt = {
        val dateString = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.format(dateFormat)

        prompt("processing") {
            system {
                markdown {
                    +"You are a helpful assistant, that thrives at answering the user's questions."
                    br()

                    h1("Output Format")
                    bulleted {
                        item("Use formal language.")
                        item("Keep responses concise and engaging unless the situation demands elaboration.")
                        item("If you use Markdown syntax, ensure the syntax is valid.")
                    }
                    br()

                    +"Today Date: $dateString"
                }
            }
        }
    },
    mcpSessionFactory = mcpSessionFactory,
) {
    // builders for state and memory
    // state can be scoped to an interaction (available in child interactions)
    // memory can be scoped to an interaction, a conversation, or a profile

    // val model by state<ModelCard> { modelManager.getModel(modelId) }
    // Type must be serializable, and with LLMDescription annotations.
    // The type of `model` is InteractiveAgent.State<ModelCard>.

    // The `onEnter`, `onLeave`, `onError`, `onEvent` and `onExecute` must provide
    // the `InteractiveAgent.State.Access` context parameter.
    // Through the `Access` interface, the state can be loaded or updated.
    // e.g., `model.load()`, `model.update(newValue)`, `model.update { previousValue -> ... }`

    // Then in graphs and functions, you have nodes and routines
    // that take `State<...>`s as parameters.

    override val initialInteraction by interaction<ConversationControl.InviteAgent, Unit>(Presence.Agent) {
        onEnter {
            act(Presence.Joining)
            act(AgentCapabilities.Messaging())
        }
        onAction<Messaging.Message> { action ->
            runInteraction(processing, action)
        }
//      onEvent<AgentState.Update> { event ->
//          if (event.property == model) {
//              model.update(modelManager.getModel(event.value))
//          }
//      }
        onLeave {
            act(Presence.Leaving)
        }
    }

    val processing by interaction<Messaging.Message, Unit>(AgentProcessing.TurnBased) {
        onEnter {
            act(AgentProcessing.Start)
        }
        onExecute { input ->
            val model = modelProviderManager.named(configuration.modelName)
            val promptExecutor = MultiLLMPromptExecutor(model.getLLMClient())
            val developerSettings = settingsRepository.getSettings(DeveloperSettingsKey).first()

            withMcpSession { mcpSession ->
                mcpSession.addServers(configuration.mcpServerNames)
                val tools = mcpSession.tools.first()

                runGraph(
                    promptExecutor = promptExecutor,
                    model = model.toLLModel(),
                    tools = tools,
                    maxAgentIterations = 50,
                    input = input
                ) {
                    val dumpRequest by dumpMessageAction()
                    val initialRequest by requestLLM()
                    val emitResponses by actOnMessages()
                    val vetToolCalls by vetToolCalls()
                    val executeTools by nodeExecuteVettedToolCalls(parallelTools = true)
                    val toolResultsRequest by nodeLLMSendMultipleToolResults()

                    edge(nodeStart forwardTo dumpRequest)
                    edge(dumpRequest forwardTo initialRequest)
                    edge(initialRequest forwardTo emitResponses)

                    edge(emitResponses forwardTo vetToolCalls onMultipleToolCalls { true })
                    edge(emitResponses forwardTo nodeFinish transformed { })

                    edge(vetToolCalls forwardTo executeTools)
                    edge(executeTools forwardTo toolResultsRequest)
                    edge(toolResultsRequest forwardTo emitResponses)
                }

            }
        }
        onError {
            act(AgentProcessing.Failure(it.message ?: "Unknown error"))
        }
        onLeave {
            act(AgentProcessing.Completion)
        }
        onAction<AgentProcessing.Cancellation> {
            act(AgentProcessing.Failure("Cancelled by user"))
            leaveInteraction()
        }
    }

    fun AIAgentSubgraphBuilderBase<*, *>.vetToolCalls(
        name: String? = null,
    ) = subgraph<List<Message.Tool.Call>, List<VettedToolCall>>(name) {
        val callsWithoutVettingKey = createStorageKey<List<Message.Tool.Call>>("vetting-tool-calls-without-vetting")

        val initiateVetting by node<List<Message.Tool.Call>, Action<ToolUsage.Vetting>> { calls ->
            val tools = withInteractionFeature { tools }

            val (callsToVet, callsWithoutVetting) = calls
                .partition { call -> tools.firstOrNull { it.name == call.tool }?.requiresVetting ?: false }

            storage.set(callsWithoutVettingKey, callsWithoutVetting)

            withInteractionFeature {
                controller.act(ToolUsage.Vetting(callsToVet.map { it.toKonvoCall() }))
            }
        }

        val vettingInteraction by runInteraction(vetting)

        val finalizeVetting by node<Map<ToolUsage.Call, Boolean?>, List<VettedToolCall>> { vettingResult ->
            val callsWithoutVetting = storage.getValue(callsWithoutVettingKey)

            callsWithoutVetting.map { VettedToolCall(it, true) } + vettingResult.map { (call, vetted) ->
                VettedToolCall(call.toKoogCall(), vetted ?: error("Tool vetting result cannot be null"))
            }
        }

        nodeStart then initiateVetting then vettingInteraction then finalizeVetting then nodeFinish
    }

    val vetting by interaction<ToolUsage.Vetting, Map<ToolUsage.Call, Boolean?>>(
        protocol = ToolUsage.VettingProtocol,
        initialState = { emptyMap() },
    ) {
        onEnter { vetting ->
            val calls = vetting.payload.calls

            val context = contextOf<AgentContext>()
            val tools = context.withMcpSession { it.tools.first() }

            updateState(
                calls.associateWith { call ->
                    tools.firstOrNull { it.name == call.tool }?.requiresVetting
                }
            )
        }

        onAction<ToolUsage.Approval> { approval ->
            updateState { statuses ->
                statuses + approval.payload.approvals.filter { (call, _) -> call in statuses }
            }

            if (state.values.all { it != null }) {
                leaveInteraction()
            }
        }
    }
}

private val dateFormat = LocalDate.Format {
    day()
    char(' ')
    monthName(MonthNames.ENGLISH_FULL)
    char(' ')
    year()
}
