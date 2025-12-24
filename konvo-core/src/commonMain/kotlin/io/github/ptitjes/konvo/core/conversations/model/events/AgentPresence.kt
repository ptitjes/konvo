package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.conversations.model.*

sealed interface AgentPresence : Event.Payload {
    data object Joining : AgentPresence, Event.Agent

    data class Available(
        val messagingCapabilities: MessagingCapabilities,
    ) : AgentPresence, Event.Agent

    data object Processing : AgentPresence, Event.Agent

    data object Leaving : AgentPresence, Event.Agent

    data class MessagingCapabilities(
        val supportedMediaTypes: List<String>,
    )
}
