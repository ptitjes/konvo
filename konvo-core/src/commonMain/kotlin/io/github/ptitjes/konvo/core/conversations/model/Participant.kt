package io.github.ptitjes.konvo.core.conversations.model

import kotlin.uuid.*

@OptIn(ExperimentalUuidApi::class)
sealed class Participant {
    data class User(val id: String, val name: String) : Participant()
    data class Agent(val id: String, val name: String) : Participant()
}
