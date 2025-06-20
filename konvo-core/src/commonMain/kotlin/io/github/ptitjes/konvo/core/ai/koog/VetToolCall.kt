package io.github.ptitjes.konvo.core.ai.koog

import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.environment.*
import ai.koog.agents.core.tools.*
import ai.koog.prompt.message.*

fun AIAgentSubgraphBuilderBase<*, *>.nodeVetToolCalls(
    name: String? = null,
    vetToolCalls: suspend (List<Message.Tool.Call>) -> List<Boolean>,
) = node<List<Message.Tool.Call>, List<VettedToolCall>>(name) { calls ->
    calls.zip(vetToolCalls(calls)).map { (call, vetted) -> VettedToolCall(call, vetted) }
}

data class VettedToolCall(
    val call: Message.Tool.Call,
    val vetted: Boolean,
)

fun AIAgentSubgraphBuilderBase<*, *>.nodeExecuteVettedToolCalls(
    name: String? = null,
    parallelTools: Boolean = false,
): AIAgentNodeDelegateBase<List<VettedToolCall>, List<ReceivedToolResult>> =
    node(name) { vettedToolCalls ->
        val (callsToExecute, callsToReject) = vettedToolCalls.partition { it.vetted }

        val executedResults = if (parallelTools) {
            environment.executeTools(callsToExecute.map { it.call })
        } else {
            callsToExecute.map { environment.executeTool(it.call) }
        }

        val rejectedResults = callsToReject.map {
            ReceivedToolResult(
                id = it.call.id,
                tool = it.call.tool,
                content = "Tool call was rejected by user",
                result = ToolResult.Text("Tool call was rejected by user"),
            )
        }

        executedResults + rejectedResults
    }
