package io.github.ptitjes.konvo.core.conversations.model

import kotlin.time.*

data class Event<out T : Event.Payload>(
    val id: String,
    val timestamp: Instant,
    val sender: Participant,
    val recipients: Set<Participant>? = null,
    val payload: T,
) {

    interface Payload

    interface Agent : Payload
    interface User : Payload
}
