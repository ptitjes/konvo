package io.github.ptitjes.konvo.plugin.core.agents.toolkit

import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.environment.*
import ai.koog.prompt.message.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.events.*

fun AIAgentSubgraphBuilderBase<*, *>.dumpMessageAction(): AIAgentNodeDelegate<Action<Messaging.Message>, Unit> =
    node { action ->
        llm.writeSession {
            val message = action.toKoogMessage()
            prompt = prompt.withMessages { messages -> messages + message }
        }
    }

@AIAgentBuilderDslMarker
fun AIAgentSubgraphBuilderBase<*, *>.requestLLM(
    name: String? = null,
) = node<Unit, List<Message.Response>>(name) {
    llm.writeSession { requestLLMMultiple() }
}

fun AIAgentSubgraphBuilderBase<*, *>.actOnMessages(): AIAgentNodeDelegate<List<Message.Response>, List<Message.Response>> =
    node { responses ->
        responses
            .filter { it.role == Message.Role.Assistant }
            .forEach { interaction.act(it.toKonvoMessage()) }
        responses
    }

inline fun <P : Action.Payload, reified S> AIAgentSubgraphBuilderBase<*, *>.runInteraction(
    interaction: InteractionDriver<P, S>,
): AIAgentNodeDelegate<Action<P>, S> = node { action ->
    withInteractionFeature {
        runInteraction(interaction, action)
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
        val toolCallId = it.call.id
        val toolName = it.call.tool
        val toolArgs = it.call.contentJson

        ReceivedToolResult(
            id = toolCallId,
            tool = toolName,
            toolArgs = toolArgs,
            toolDescription = null,
            content = "Tool call with name '$toolName' was rejected by user",
            resultKind = ToolResultKind.Failure(null),
            result = null,
        )
    }

    executedResults + rejectedResults
}
