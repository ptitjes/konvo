package io.github.ptitjes.konvo.core.conversations.model

import kotlinx.serialization.*

@Serializable
sealed class Participant {
    @Serializable
    @SerialName("user")
    data class User(val id: String, val name: String) : Participant()

    @Serializable
    @SerialName("agent")
    data class Agent(val id: String, val name: String) : Participant()
}
