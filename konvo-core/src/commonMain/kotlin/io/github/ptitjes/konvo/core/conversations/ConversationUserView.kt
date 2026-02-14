package io.github.ptitjes.konvo.core.conversations

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.coroutines.flow.*

/**
 * Provides a view of the conversation for UI components.
 * This interface allows UI components to send user events into a conversation.
 */
interface ConversationUserView {

    val participant: Participant.User

    val state: StateFlow<ConversationState>

    val events: SharedFlow<Event<*>>

    suspend fun send(payload: Event.User)
}
