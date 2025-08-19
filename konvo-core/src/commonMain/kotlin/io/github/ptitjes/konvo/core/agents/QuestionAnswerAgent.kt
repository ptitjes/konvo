package io.github.ptitjes.konvo.core.agents

import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.core.tools.*
import ai.koog.agents.features.eventHandler.feature.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.llms.*
import ai.koog.prompt.markdown.*
import ai.koog.prompt.message.*
import io.github.ptitjes.konvo.core.agents.toolkit.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.tools.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import kotlinx.datetime.format.*
import kotlin.coroutines.*
import kotlin.time.*
import kotlin.time.Clock
import kotlin.uuid.*

fun buildQuestionAnswerAgent(
    model: ModelCard,
    mcpSessionFactory: (coroutineContext: CoroutineContext) -> McpHostSession,
    mcpServerNames: Set<String>,
): Agent {
    return DefaultAgent(
        systemPrompt = buildSystemPrompt(),
        model = model.toLLModel(),
        promptExecutor = CallFixingPromptExecutor(SingleLLMPromptExecutor(model.getLLMClient())),
        strategy = { conversationView ->
            strategy("qa") {
                val qa by qaWithTools { calls -> conversationView.vetToolCalls(calls, emptyList()) }
                nodeStart then qa then nodeFinish
            }
        },
        mcpSessionFactory = mcpSessionFactory,
        mcpServerNames = mcpServerNames,
    ) { conversationView ->
        install(EventHandler) {
            onToolValidationError { eventContext ->
                @Suppress("UNCHECKED_CAST") val broaderTool = eventContext.tool as Tool<ToolArgs, ToolResult>
                conversationView.sendToolUseResult(
                    call = ToolCall(
                        id = eventContext.toolCallId ?: newUniqueId(),
                        tool = broaderTool.name,
                        arguments = broaderTool.encodeArgs(eventContext.toolArgs)
                    ),
                    result = ToolCallResult.ExecutionFailure(eventContext.error),
                )
            }
            onToolCallResult { eventContext ->
                @Suppress("UNCHECKED_CAST") val broaderTool = eventContext.tool as Tool<ToolArgs, ToolResult>
                conversationView.sendToolUseResult(
                    call = ToolCall(
                        id = eventContext.toolCallId ?: newUniqueId(),
                        tool = broaderTool.name,
                        arguments = broaderTool.encodeArgs(eventContext.toolArgs)
                    ),
                    result = ToolCallResult.Success(eventContext.result.toResultText()),
                )
            }
            onToolCallFailure { eventContext ->
                @Suppress("UNCHECKED_CAST") val broaderTool = eventContext.tool as Tool<ToolArgs, ToolResult>
                conversationView.sendToolUseResult(
                    call = ToolCall(
                        id = eventContext.toolCallId ?: newUniqueId(),
                        tool = broaderTool.name,
                        arguments = broaderTool.encodeArgs(eventContext.toolArgs)
                    ),
                    result = ToolCallResult.ExecutionFailure(eventContext.throwable.message ?: "Tool failed"),
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun buildSystemPrompt(): Prompt {
    val dateString = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.format(dateFormat)

    return prompt("qa") {
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
}

private val dateFormat = LocalDate.Format {
    day()
    char(' ')
    monthName(MonthNames.ENGLISH_FULL)
    char(' ')
    year()
}

private fun ToolResult?.toResultText(): String = when (this) {
    is ToolResult.Text -> this.text
    is ToolResult.JSONSerializable<*> -> this.toStringDefault()
    else -> "Tool succeeded"
}

private suspend fun ConversationAgentView.vetToolCalls(
    calls: List<Message.Tool.Call>,
    tools: List<ToolCard>,
): List<Boolean> {
    val vettedCalls = calls.map { CompletableDeferred<Boolean>() }

    val (withVetting, withoutVetting) = calls.withIndex().partition { (_, call) ->
        tools.firstOrNull { it.name == call.tool }?.requiresVetting ?: false
    }

    withoutVetting.forEach { (index, _) -> vettedCalls[index].complete(true) }

    val vetoableToolCalls = withVetting.map { (index, call) ->
        index to ToolCall(
            id = call.id ?: newUniqueId(),
            tool = call.tool,
            arguments = call.contentJson,
        )
    }

    if (vetoableToolCalls.isEmpty()) return vettedCalls.awaitAll()

    val vettingEvent = sendToolUseVetting(vetoableToolCalls.map { it.second })

    sendProcessing(false)

    val approvalsEvent = events.filterIsInstance<Event.ToolUseApproval>().first { it.vetting == vettingEvent }
    val approvalsByCall = approvalsEvent.approvals

    vetoableToolCalls.forEach { (index, call) ->
        vettedCalls[index].complete(approvalsByCall[call] == true)
    }

    return vettedCalls.awaitAll().also {
        sendProcessing(true)
    }
}

private fun AIAgentSubgraphBuilderBase<*, *>.qaWithTools(
    vetToolCalls: suspend (List<Message.Tool.Call>) -> List<Boolean>,
) = subgraph<Message.User, List<Message.Assistant>> {
    val dumpInitialRequest by dumpToPrompt()
    val initialRequest by requestLLM()
    val processResponses by nodeDoNothing<List<Message.Response>>()
    val vetToolCalls by nodeVetToolCalls(vetToolCalls = vetToolCalls)
    val executeTools by nodeExecuteVettedToolCalls(parallelTools = true)
    val toolResultsRequest by nodeLLMSendMultipleToolResults()

    edge(nodeStart forwardTo dumpInitialRequest)
    edge(dumpInitialRequest forwardTo initialRequest)
    edge(initialRequest forwardTo processResponses)

    edge(processResponses forwardTo vetToolCalls onMultipleToolCalls { true })
    edge(processResponses forwardTo nodeFinish onMultipleAssistantMessages { true })

    edge(vetToolCalls forwardTo executeTools)
    edge(executeTools forwardTo toolResultsRequest)
    edge(toolResultsRequest forwardTo processResponses)
}

@OptIn(ExperimentalUuidApi::class)
private fun newUniqueId(): String = Uuid.random().toString()
