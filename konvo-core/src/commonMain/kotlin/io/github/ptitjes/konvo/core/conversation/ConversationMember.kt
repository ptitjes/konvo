package io.github.ptitjes.konvo.core.conversation

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed class ConversationMember {
    data class User(val id: String, val name: String) : ConversationMember()
    data class Agent(val id: String, val name: String) : ConversationMember()
}
