package io.github.ptitjes.konvo.core.conversations

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.coroutines.flow.*

/**
 * Provides a view of the conversation for UI components.
 * This interface allows UI components to send user events into a conversation.
 */
interface ConversationUserView {

    val state: StateFlow<ConversationState>

    val events: SharedFlow<Event>

    suspend fun updateTitle(title: String)

    /**
     * Update the last read message index.
     * Implementations should clamp the value to the current bounds and ignore decreases that are out of range.
     */
    suspend fun updateLastReadMessageIndex(index: Int)

    suspend fun sendMessage(
        content: List<ContentPart>,
    )

    suspend fun sendToolUseApproval(
        approvals: Map<ToolCall, Boolean>,
    )

    suspend fun send(payload: Event.User)
}
