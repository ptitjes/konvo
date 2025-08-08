@file:OptIn(ExperimentalUuidApi::class)
package io.github.ptitjes.konvo.core.conversation

import kotlinx.coroutines.flow.SharedFlow
import kotlin.uuid.*

/**
 * Provides a view of the conversation for UI components.
 * This interface allows UI components to send user events and receive assistant events.
 */
interface ConversationUserView {
    /**
     * Flow of assistant events from the conversation.
     */
    val events: SharedFlow<ConversationEvent>

    suspend fun sendMessage(
        content: String,
        attachments: List<Attachment> = emptyList(),
    )

    suspend fun sendToolUseApproval(
        vetting: ConversationEvent.AssistantToolUseVetting,
        approvals: Map<ToolCall, Boolean>,
    )
}
