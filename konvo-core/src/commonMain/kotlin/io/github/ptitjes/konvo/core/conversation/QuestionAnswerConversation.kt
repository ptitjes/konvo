package io.github.ptitjes.konvo.core.conversation

import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.core.environment.*
import ai.koog.agents.core.tools.*
import ai.koog.agents.features.eventHandler.feature.*
import ai.koog.prompt.executor.llms.*
import ai.koog.prompt.message.*
import io.github.ptitjes.konvo.core.ai.koog.*
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
            promptExecutor = SingleLLMPromptExecutor(model.getLLMClient()),
            strategy = strategy("qa") {
                val qa by qaWithTools { call ->
                    val toolCard = tools.first { it.name == call.tool }
                    if (toolCard.requiresVetting) vetToolCall(call) else true
                }
                nodeStart then qa then nodeFinish
            },
            initialToolRegistry = toolRegistry,
        ) {
            install(EventHandler) {
                onToolValidationError = { tool, toolArgs, value ->
                    @Suppress("UNCHECKED_CAST") val broaderTool = tool as Tool<Tool.Args, ToolResult>
                    sendAssistantEvent(
                        AssistantEvent.ToolUseResult(
                            tool = tool.name,
                            arguments = broaderTool.encodeArgs(toolArgs),
                            result = ToolCallResult.ExecutionFailure(value),
                        )
                    )
                }
                onToolCallResult = { tool, toolArgs, result ->
                    @Suppress("UNCHECKED_CAST") val broaderTool = tool as Tool<Tool.Args, ToolResult>
                    sendAssistantEvent(
                        AssistantEvent.ToolUseResult(
                            tool = tool.name,
                            arguments = broaderTool.encodeArgs(toolArgs),
                            result = ToolCallResult.Success(result?.toStringDefault() ?: "Tool succeeded"),
                        )
                    )
                }
                onToolCallFailure = { tool, toolArgs, throwable ->
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

    private suspend fun vetToolCall(call: Message.Tool.Call): Boolean {
        val vetted = CompletableDeferred<Boolean>()

        val vetoableToolCall = object : VetoableToolCall {
            override val tool: String get() = call.tool
            override val arguments: Map<String, JsonElement> = call.contentJson

            override fun allow() {
                vetted.complete(true)
            }

            override fun reject() {
                vetted.complete(false)
            }
        }

        sendAssistantEvent(AssistantEvent.ToolUsePermission(listOf(vetoableToolCall)))

        return vetted.await()
    }
}

private fun AIAgentSubgraphBuilderBase<*, *>.qaWithTools(
    vetToolCall: suspend (Message.Tool.Call) -> Boolean,
) = subgraph {
    val initialRequest by nodeLLMRequest()
    val executeTool by nodeExecuteTool()
    val toolResultRequest by nodeLLMSendToolResult()
    val maybeFixToolCall by nodeMaybeFixToolCall()
    val vetToolCall by nodeVetToolCall(vetToolCall = vetToolCall)

    edge(nodeStart forwardTo initialRequest)
    edge(initialRequest forwardTo maybeFixToolCall)

    edge(maybeFixToolCall forwardTo vetToolCall onToolCall { true })
    edge(maybeFixToolCall forwardTo nodeFinish onAssistantMessage { true })

    edge(vetToolCall forwardTo executeTool onCondition { it.vetted } transformed { it.call })
    edge(vetToolCall forwardTo toolResultRequest onCondition { !it.vetted } transformed {
        ReceivedToolResult(
            id = it.call.id,
            tool = it.call.tool,
            content = "Tool call was rejected by user",
            result = ToolResult.Text("Tool call was rejected by user"),
        )
    })

    edge(executeTool forwardTo toolResultRequest)
    edge(toolResultRequest forwardTo maybeFixToolCall)
}
