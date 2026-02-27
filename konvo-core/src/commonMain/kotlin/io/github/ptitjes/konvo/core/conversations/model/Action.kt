package io.github.ptitjes.konvo.core.conversations.model

import kotlinx.serialization.*
import kotlin.time.*

data class Action<out T : Action.Payload>(
    val id: String,
    val timestamp: Instant,
    val sender: Participant,
    val recipients: Set<Participant>? = null,
    val payload: @Contextual T,
) {

    interface Payload

    interface Agent : Payload

    interface User : Payload
}
