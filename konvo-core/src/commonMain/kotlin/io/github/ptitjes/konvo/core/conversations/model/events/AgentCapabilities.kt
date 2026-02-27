package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.conversations.model.Action
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface AgentCapabilities : Action.Payload {

    @Serializable
    @SerialName("agent-capabilities-messaging")
    data class Messaging(
        val supportedMediaTypes: List<String> = emptyList(),
    ) : AgentCapabilities, Action.Agent
}
