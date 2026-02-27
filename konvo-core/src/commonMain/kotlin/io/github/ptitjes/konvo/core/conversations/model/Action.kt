package io.github.ptitjes.konvo.core.conversations.model

import kotlinx.serialization.*
import kotlin.time.*

data class Action<out T : Action.Payload>(
    val id: String,
    override val timestamp: Instant,
    override val sender: Participant,
    val recipients: Set<Participant>? = null,
    val payload: @Contextual T,
) : ConversationEntry(timestamp, sender) {

    interface Payload

    interface Agent : Payload

    interface User : Payload
}
