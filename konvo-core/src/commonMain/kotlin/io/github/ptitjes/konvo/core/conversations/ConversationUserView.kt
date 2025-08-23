package io.github.ptitjes.konvo.core.conversations

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.coroutines.flow.*

/**
 * Provides a view of the conversation for UI components.
 * This interface allows UI components to send user events into a conversation.
 */
interface ConversationUserView {

    val state: StateFlow<ConversationState>

    suspend fun updateTitle(title: String)

    /**
     * Update the last read message index.
     * Implementations should clamp the value to the current bounds and ignore decreases that are out of range.
     */
    suspend fun updateLastReadMessageIndex(index: Int)

    suspend fun sendMessage(
        content: String,
        attachments: List<Attachment> = emptyList(),
    )

    suspend fun sendToolUseApproval(
        vetting: Event.ToolUseVetting,
        approvals: Map<ToolCall, Boolean>,
    )
}
