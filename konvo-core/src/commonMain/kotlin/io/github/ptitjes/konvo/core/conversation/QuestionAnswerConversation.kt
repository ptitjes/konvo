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

suspend fun buildQuestionAnswerAgent(configuration: QuestionAnswerAgentConfiguration): ChatAgent {
    val model = configuration.model

    val tools = configuration.tools
    val toolRegistry = tools.map { it.toTool() }.let { ToolRegistry { tools(it) } }

    return ChatAgent(
        systemPrompt = configuration.prompt.toPrompt(),
        model = model.toLLModel(),
        maxAgentIterations = 50,
        promptExecutor = CallFixingPromptExecutor(SingleLLMPromptExecutor(model.getLLMClient())),
        strategy = { conversation ->
            strategy("qa") {
                val qa by qaWithTools { calls -> conversation.vetToolCalls(calls, tools) }
                nodeStart then qa then nodeFinish
            }
        },
        initialToolRegistry = toolRegistry,
    ) { conversation ->
        install(EventHandler) {
            onToolValidationError { eventContext ->
                @Suppress("UNCHECKED_CAST") val broaderTool = eventContext.tool as Tool<ToolArgs, ToolResult>
                conversation.sendAssistantEvent(
                    AssistantEvent.ToolUseResult(
                        tool = broaderTool.name,
                        arguments = broaderTool.encodeArgs(eventContext.toolArgs),
                        result = ToolCallResult.ExecutionFailure(eventContext.error),
                    )
                )
            }
            onToolCallResult { eventContext ->
                @Suppress("UNCHECKED_CAST") val broaderTool = eventContext.tool as Tool<ToolArgs, ToolResult>
                conversation.sendAssistantEvent(
                    AssistantEvent.ToolUseResult(
                        tool = broaderTool.name,
                        arguments = broaderTool.encodeArgs(eventContext.toolArgs),
                        result = ToolCallResult.Success(eventContext.result?.toStringDefault() ?: "Tool succeeded"),
                    )
                )
            }
            onToolCallFailure { eventContext ->
                @Suppress("UNCHECKED_CAST") val broaderTool = eventContext.tool as Tool<ToolArgs, ToolResult>
                conversation.sendAssistantEvent(
                    AssistantEvent.ToolUseResult(
                        tool = broaderTool.name,
                        arguments = broaderTool.encodeArgs(eventContext.toolArgs),
                        result = ToolCallResult.ExecutionFailure(eventContext.throwable.message ?: "Tool failed"),
                    )
                )
            }
        }
    }
}

private suspend fun Conversation.vetToolCalls(calls: List<Message.Tool.Call>, tools: List<ToolCard>): List<Boolean> {
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
