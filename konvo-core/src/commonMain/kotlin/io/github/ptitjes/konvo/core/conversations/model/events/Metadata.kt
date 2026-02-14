package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.serialization.*

@Serializable
sealed interface Metadata : Event.Payload {
    @Serializable
    @SerialName("metadata-title-change")
    data class TitleChange(val title: String) : Metadata, Event.Agent, Event.User
}
