package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.serialization.*

@Serializable
sealed interface AgentPresence : Event.Payload {
    @Serializable
    @SerialName("agent-presence-joining")
    data object Joining : AgentPresence, Event.Agent

    @Serializable
    @SerialName("agent-presence-leaving")
    data object Leaving : AgentPresence, Event.Agent
}
