package io.github.ptitjes.konvo.core.conversations

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.coroutines.flow.*

/**
 * Provides a view of the conversation for AI agents.
 * This interface allows agents to send assistant events into a conversation.
 */
interface ConversationAgentView {

    val events: SharedFlow<Event<*>>

    suspend fun send(payload: Event.Agent)
}
