package io.github.ptitjes.konvo.core.conversation

/**
 * Provides a view of the conversation for UI components.
 * This interface allows UI components to send user events into a conversation.
 */
interface ConversationUserView {
    /**
     * Reference to the parent conversation.
     */
    val conversation: ActiveConversation

    suspend fun sendMessage(
        content: String,
        attachments: List<Attachment> = emptyList(),
    )

    suspend fun sendToolUseApproval(
        vetting: ConversationEvent.AssistantToolUseVetting,
        approvals: Map<ToolCall, Boolean>,
    )
}
