package io.github.ptitjes.konvo.core.conversation

import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.core.tools.*
import ai.koog.agents.features.eventHandler.feature.*
import ai.koog.prompt.executor.llms.*
import ai.koog.prompt.message.*
import io.github.ptitjes.konvo.core.ai.koog.*
import io.github.ptitjes.konvo.core.ai.spi.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

class QuestionAnswerConversation(
    coroutineScope: CoroutineScope,
    override val configuration: QuestionAnswerModeConfiguration,
) : TurnBasedConversation(coroutineScope) {
    override suspend fun buildChatAgent(): ChatAgent {
        val model = configuration.model

        val tools = configuration.tools
        val toolRegistry = tools.map { it.toTool() }.let { ToolRegistry { tools(it) } }

        return ChatAgent(
            initialPrompt = configuration.prompt.toPrompt(),
            model = model.toLLModel(),
            maxAgentIterations = 50,
            promptExecutor = CallFixingPromptExecutor(SingleLLMPromptExecutor(model.getLLMClient())),
            strategy = strategy("qa") {
                val qa by qaWithTools { calls -> vetToolCalls(calls, tools) }
                nodeStart then qa then nodeFinish
            },
            initialToolRegistry = toolRegistry,
        ) {
            install(EventHandler) {
                onToolValidationError { tool, toolArgs, value ->
                    @Suppress("UNCHECKED_CAST") val broaderTool = tool as Tool<Tool.Args, ToolResult>
                    sendAssistantEvent(
                        AssistantEvent.ToolUseResult(
                            tool = tool.name,
                            arguments = broaderTool.encodeArgs(toolArgs),
                            result = ToolCallResult.ExecutionFailure(value),
                        )
                    )
                }
                onToolCallResult { tool, toolArgs, result ->
                    @Suppress("UNCHECKED_CAST") val broaderTool = tool as Tool<Tool.Args, ToolResult>
                    sendAssistantEvent(
                        AssistantEvent.ToolUseResult(
                            tool = tool.name,
                            arguments = broaderTool.encodeArgs(toolArgs),
                            result = ToolCallResult.Success(result?.toStringDefault() ?: "Tool succeeded"),
                        )
                    )
                }
                onToolCallFailure { tool, toolArgs, throwable ->
                    @Suppress("UNCHECKED_CAST") val broaderTool = tool as Tool<Tool.Args, ToolResult>
                    sendAssistantEvent(
                        AssistantEvent.ToolUseResult(
                            tool = tool.name,
                            arguments = broaderTool.encodeArgs(toolArgs),
                            result = ToolCallResult.ExecutionFailure(throwable.message ?: "Tool failed"),
                        )
                    )
                }
            }
        }
    }

    private suspend fun vetToolCalls(calls: List<Message.Tool.Call>, tools: List<ToolCard>): List<Boolean> {
        val vettedCalls = calls.map { CompletableDeferred<Boolean>() }

        val (withVetting, withoutVetting) = calls.withIndex().partition { (_, call) ->
            tools.first { it.name == call.tool }.requiresVetting
        }

        withoutVetting.forEach { (index, _) -> vettedCalls[index].complete(true) }

        val vetoableToolCalls = withVetting.map { (index, call) ->
            object : VetoableToolCall {
                override val tool: String get() = call.tool
                override val arguments: Map<String, JsonElement> = call.contentJson

                override fun allow() {
                    vettedCalls[index].complete(true)
                }

                override fun reject() {
                    vettedCalls[index].complete(false)
                }
            }
        }

        if (vetoableToolCalls.isNotEmpty()) {
            sendAssistantEvent(AssistantEvent.ToolUseVetting(vetoableToolCalls))
        }

        return vettedCalls.awaitAll()
    }
}

private fun AIAgentSubgraphBuilderBase<*, *>.qaWithTools(
    vetToolCalls: suspend (List<Message.Tool.Call>) -> List<Boolean>,
) = subgraph {
    val initialRequest by nodeLLMRequestMultiple()
    val processResponses by nodeDoNothing<List<Message.Response>>()
    val vetToolCalls by nodeVetToolCalls(vetToolCalls = vetToolCalls)
    val executeTools by nodeExecuteVettedToolCalls(parallelTools = true)
    val toolResultsRequest by nodeLLMSendMultipleToolResults()

    edge(nodeStart forwardTo initialRequest)
    edge(initialRequest forwardTo processResponses)

    edge(processResponses forwardTo vetToolCalls onAnyToolCalls { true })
    edge(processResponses forwardTo nodeFinish transformed { it.first() } onAssistantMessage { true })

    edge(vetToolCalls forwardTo executeTools)
    edge(executeTools forwardTo toolResultsRequest)
    edge(toolResultsRequest forwardTo processResponses)
}

infix fun <IncomingOutput, IntermediateOutput, OutgoingInput>
        AIAgentEdgeBuilderIntermediate<IncomingOutput, IntermediateOutput, OutgoingInput>.onAnyToolCalls(
    block: suspend (List<Message.Tool.Call>) -> Boolean
): AIAgentEdgeBuilderIntermediate<IncomingOutput, List<Message.Tool.Call>, OutgoingInput> {
    return onIsInstance(List::class)
        .transformed { it.filterIsInstance<Message.Tool.Call>() }
        .onCondition { toolCalls -> toolCalls.isNotEmpty() && block(toolCalls) }
}
