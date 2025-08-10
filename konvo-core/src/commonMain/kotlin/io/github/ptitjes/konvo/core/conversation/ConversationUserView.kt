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
    val conversation: ActiveConversation

    val transcript: Transcript

    val events: SharedFlow<Event>

    suspend fun sendMessage(
        content: String,
        attachments: List<Attachment> = emptyList(),
    )

    suspend fun sendToolUseApproval(
        vetting: Event.ToolUseVetting,
        approvals: Map<ToolCall, Boolean>,
    )
}
