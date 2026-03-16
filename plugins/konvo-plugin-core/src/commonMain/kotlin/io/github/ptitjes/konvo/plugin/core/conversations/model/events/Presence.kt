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
    @SerialName("presence-joining")
    data object Joining : Presence, Action.Agent, Action.User

    @Serializable
    @SerialName("presence-leaving")
    data object Leaving : Presence, Action.Agent, Action.User

    @Serializable
    @SerialName("presence-view-notification")
    data class ViewNotification(val upToTimestamp: Instant) : Presence, Action.User
}
