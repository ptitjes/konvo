package io.github.ptitjes.konvo.core.conversation

import io.github.ptitjes.konvo.core.conversation.model.*
import kotlinx.coroutines.flow.*

/**
 * Provides a view of the conversation for UI components.
 * This interface allows UI components to send user events into a conversation.
 */
interface ConversationUserView {
    /**
     * Reference to the parent conversation.
     */
    val conversation: LiveConversation

    val transcript: Transcript

    val events: SharedFlow<Event>

    /**
     * The index of the last read message in the current UI transcript, or -1 if none read.
     * This is a live state that can be observed to update UI markers.
     */
    val lastReadMessageIndex: StateFlow<Int>

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
