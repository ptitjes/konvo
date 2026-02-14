package io.github.ptitjes.konvo.core.conversations.model

import kotlinx.serialization.*

@Serializable
sealed interface Participant {
    val id: String
    val name: String

    @Serializable
    @SerialName("user")
    data class User(override val id: String, override val name: String) : Participant

    @Serializable
    @SerialName("agent")
    data class Agent(override val id: String, override val name: String) : Participant
}
