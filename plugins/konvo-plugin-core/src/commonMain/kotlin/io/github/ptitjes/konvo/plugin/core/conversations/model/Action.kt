package io.github.ptitjes.konvo.plugin.core.conversations.model

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

@Suppress("UNCHECKED_CAST")
inline fun <reified P : Action.Payload> Action<*>.asTypedAction(): Action<P> {
    check(payload is P) { "Payload is not of expected type ${P::class.simpleName}" }
    return this as Action<P>
}

@Suppress("UNCHECKED_CAST")
inline fun <reified P : Action.Payload> Action<*>.asTypedActionOrNull(): Action<P>? {
    if (payload !is P) return null
    return this as Action<P>
}
