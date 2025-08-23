package io.github.ptitjes.konvo.core.conversations.model

sealed class Participant {
    data class User(val id: String, val name: String) : Participant()
    data class Agent(val id: String, val name: String) : Participant()
}
