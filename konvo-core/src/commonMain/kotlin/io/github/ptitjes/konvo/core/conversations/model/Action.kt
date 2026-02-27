package io.github.ptitjes.konvo.core.conversations.model

import kotlin.time.*

data class Action<out T : Action.Payload>(
    val id: String,
    override val timestamp: Instant,
    override val sender: Participant,
    val recipients: Set<Participant>? = null,
    val interaction: Interaction? = null,
    val payload: T,
) : ConversationEntry(timestamp, sender) {

    interface Payload

    interface Agent : Payload

    interface User : Payload
}
