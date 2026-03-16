package io.github.ptitjes.konvo.plugin.core.conversations.model

import kotlinx.serialization.*

@Serializable
sealed interface Participant {
    val id: String

    @JvmInline
    @Serializable
    @SerialName("user")
    value class User(override val id: String) : Participant

    @JvmInline
    @Serializable
    @SerialName("agent")
    value class Agent(override val id: String) : Participant
}
