package io.github.ptitjes.konvo.core.agents.toolkit

import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.environment.*
import ai.koog.prompt.message.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.core.conversations.model.events.ToolUsage.Call
import io.github.ptitjes.konvo.core.tools.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import kotlin.uuid.*

@AIAgentBuilderDslMarker
fun AIAgentSubgraphBuilderBase<*, *>.dumpToPrompt(
    name: String? = null,
) = node<Message.User, Unit>(name) { message ->
    llm.writeSession {
        prompt = prompt.withMessages { messages -> messages + message }
    }
}

@AIAgentBuilderDslMarker
fun AIAgentSubgraphBuilderBase<*, *>.requestLLM(
    name: String? = null,
) = node<Unit, List<Message.Response>>(name) {
    llm.writeSession { requestLLMMultiple() }
}

@AIAgentBuilderDslMarker
fun AIAgentSubgraphBuilderBase<*, *>.nodeVetToolCalls(
    name: String? = null,
) = node<List<Message.Tool.Call>, List<VettedToolCall>>(name) { calls ->
    withConversationFeature {
        calls.zip(view.vetToolCalls(calls, tools)).map { (call, vetted) -> VettedToolCall(call, vetted) }
    }
}

internal suspend fun ConversationAgentView.vetToolCalls(
    calls: List<Message.Tool.Call>,
    tools: List<ToolCard>,
): List<Boolean> = coroutineScope {
    val vettedCalls = calls.map { CompletableDeferred<Boolean>() }

    val (withVetting, withoutVetting) = calls.withIndex().partition { (_, call) ->
        tools.firstOrNull { it.name == call.tool }?.requiresVetting ?: false
    }

    withoutVetting.forEach { (index, _) -> vettedCalls[index].complete(true) }

    val vetoableToolCalls = withVetting.associate { (index, call) ->
        Call(
            id = call.id ?: newUniqueId(),
            tool = call.tool,
            arguments = call.contentJson,
        ) to index
    }

    if (vetoableToolCalls.isEmpty()) return@coroutineScope vettedCalls.awaitAll()

    sendToolUseVetting(vetoableToolCalls.keys.toList())

    sendProcessing(false)

    val updateJob = launch {
        events.mapNotNull { it.payload as? ToolUsage.Approval }.collect { payload ->
            for ((call, approved) in payload.approvals) {
                val index = vetoableToolCalls[call]
                if (index != null) {
                    vettedCalls[index].complete(approved)
                }
            }
        }
    }

    vettedCalls.awaitAll().also {
        updateJob.cancel()
        sendProcessing(true)
    }
}

data class VettedToolCall(
    val call: Message.Tool.Call,
    val vetted: Boolean,
)

@AIAgentBuilderDslMarker
fun AIAgentSubgraphBuilderBase<*, *>.nodeExecuteVettedToolCalls(
    name: String? = null,
    parallelTools: Boolean = false,
) = node<List<VettedToolCall>, List<ReceivedToolResult>>(name) { vettedToolCalls ->
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
            result = JsonPrimitive("Tool call was rejected by user"),
        )
    }

    executedResults + rejectedResults
}

private fun newUniqueId(): String = Uuid.random().toString()
