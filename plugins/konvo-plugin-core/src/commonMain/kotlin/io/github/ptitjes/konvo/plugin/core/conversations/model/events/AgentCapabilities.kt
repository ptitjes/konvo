package io.github.ptitjes.konvo.plugin.core.conversations.model.events

import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import kotlinx.serialization.*

@Serializable
sealed interface AgentCapabilities : Action.Payload {

    @Serializable
    @SerialName("agent-capabilities-messaging")
    data class Messaging(
        val supportedMediaTypes: List<String> = emptyList(),
    ) : AgentCapabilities, Action.Agent
}
