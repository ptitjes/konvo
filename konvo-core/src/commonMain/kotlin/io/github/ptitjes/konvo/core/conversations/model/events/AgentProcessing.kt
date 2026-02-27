package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.conversations.model.Action
import io.github.ptitjes.konvo.core.conversations.model.InteractionProtocol
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal const val PLUGIN_ID = "konvo:io.github.ptitjes.konvo.core"

@Serializable
sealed interface AgentProcessing : Action.Payload {

    @Serializable
    @SerialName("$PREFIX#Start")
    data object Start : AgentProcessing, Action.Agent

    @Serializable
    @SerialName("agent-processing-cancellation")
    data object Cancellation : AgentProcessing, Action.User

    @Serializable
    @SerialName("agent-processing-failure")
    data class Failure(val reason: String) : AgentProcessing, Action.Agent

    @Serializable
    @SerialName("agent-processing-completion")
    data object Completion : AgentProcessing, Action.Agent

    companion object {
        private const val PREFIX = "$PLUGIN_ID/AgentProcessing"

        val TurnBased = InteractionProtocol(
            id = "$PREFIX#TurnBased",
            awaitsInput = true,
            hidesParent = true,
            reactsTo = setOf(Cancellation::class),
        )
    }
}
