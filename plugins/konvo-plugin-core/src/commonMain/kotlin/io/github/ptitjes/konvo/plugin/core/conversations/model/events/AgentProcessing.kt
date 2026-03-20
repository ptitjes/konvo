package io.github.ptitjes.konvo.plugin.core.conversations.model.events

import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import kotlinx.serialization.*

@Serializable
sealed interface AgentProcessing : Action.Payload {

    @Serializable
    @SerialName("AgentProcessing#TurnBased")
    data object Start : AgentProcessing, Action.Agent

    @Serializable
    @SerialName("AgentProcessing#Cancellation")
    data object Cancellation : AgentProcessing, Action.User

    @Serializable
    @SerialName("AgentProcessing#Failure")
    data class Failure(val reason: String) : AgentProcessing, Action.Agent

    @Serializable
    @SerialName("AgentProcessing#Completion")
    data object Completion : AgentProcessing, Action.Agent

    companion object {
        val TurnBased = InteractionProtocol(
            id = "AgentProcessing#TurnBased",
            awaitsInput = false,
            hidesParent = true,
            reactsTo = setOf(Cancellation::class),
        )
    }
}
