package io.github.ptitjes.konvo.plugin.core.conversations.model.events

import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import kotlinx.serialization.*
import kotlin.time.*

/*
TODO Split again?
AgentPresence
    Joining
    Leaving
UserPresence
    Joining
    Leaving
    TypingIndication
    ViewIndication
 */
@Serializable
sealed interface Presence : Action.Payload {
    @Serializable
    @SerialName("Presence#Joining")
    data object Joining : Presence, Action.Agent, Action.User

    @Serializable
    @SerialName("Presence#Leaving")
    data object Leaving : Presence, Action.Agent, Action.User

    @Serializable
    @SerialName("Presence#View")
    data class ViewNotification(val upToTimestamp: Instant) : Presence, Action.User

    companion object {
        val Agent = InteractionProtocol(
            id = "Presence#Agent",
            awaitsInput = true,
            hidesParent = false,
            reactsTo = setOf(Messaging.Message::class),
        )
    }
}
