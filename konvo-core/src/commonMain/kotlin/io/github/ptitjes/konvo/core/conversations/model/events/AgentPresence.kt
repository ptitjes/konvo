package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.conversations.model.*

sealed interface AgentPresence : Event.Payload {

    data class Processing(
        val isProcessing: Boolean,
    ) : AgentPresence, Event.Agent
}
