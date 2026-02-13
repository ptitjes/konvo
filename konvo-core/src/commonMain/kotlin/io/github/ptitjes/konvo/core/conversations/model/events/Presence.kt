package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.serialization.*
import kotlin.time.Instant

@Serializable
sealed interface Presence : Event.Payload {
    @Serializable
    @SerialName("presence-joining")
    data object Joining : Presence, Event.Agent, Event.User

    @Serializable
    @SerialName("presence-leaving")
    data object Leaving : Presence, Event.Agent, Event.User

    @Serializable
    @SerialName("presence-view-notification")
    data class ViewNotification(val upToTimestamp: Instant) : Presence, Event.User
}
