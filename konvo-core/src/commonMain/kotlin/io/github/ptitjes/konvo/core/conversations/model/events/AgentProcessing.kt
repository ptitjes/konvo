package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.conversations.model.Event
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface AgentProcessing : Event.Payload {

    @Serializable
    @SerialName("agent-processing-start")
    data object Start : AgentProcessing, Event.Agent

    @Serializable
    @SerialName("agent-processing-cancellation")
    data object Cancellation : AgentProcessing, Event.User

    @Serializable
    @SerialName("agent-processing-failure")
    data class Failure(val reason: String) : AgentProcessing, Event.Agent

    @Serializable
    @SerialName("agent-processing-completion")
    data object Completion : AgentProcessing, Event.Agent
}
