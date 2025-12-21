package io.github.ptitjes.konvo.core.conversations

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.Part
import io.github.ptitjes.konvo.core.conversations.model.events.ToolUsage.Call
import kotlinx.coroutines.flow.*

/**
 * Provides a view of the conversation for UI components.
 * This interface allows UI components to send user events into a conversation.
 */
interface ConversationUserView {

    val state: StateFlow<ConversationState>

    val events: SharedFlow<Event<*>>

    suspend fun updateTitle(title: String)

    /**
     * Update the last read message index.
     * Implementations should clamp the value to the current bounds and ignore decreases that are out of range.
     */
    suspend fun updateLastReadMessageIndex(index: Int)

    suspend fun sendMessage(
        content: List<Part>,
    )

    suspend fun sendToolUseApproval(
        approvals: Map<Call, Boolean>,
    )

    suspend fun send(payload: Event.User)
}
