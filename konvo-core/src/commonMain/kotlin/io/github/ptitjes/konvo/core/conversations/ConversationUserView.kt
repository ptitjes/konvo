package io.github.ptitjes.konvo.core.conversations

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.*
import io.github.ptitjes.konvo.core.conversations.model.events.ToolUsage.*
import kotlinx.coroutines.flow.*

/**
 * Provides a view of the conversation for UI components.
 * This interface allows UI components to send user events into a conversation.
 */
interface ConversationUserView {

    val participant: Participant.User

    val state: StateFlow<ConversationState>

    val events: SharedFlow<Event<*>>

    suspend fun updateTitle(title: String)

    /**
     * Update the last read message index.
     * Implementations should clamp the value to the current bounds and ignore decreases that are out of range.
     */
    suspend fun updateLastReadMessageIndex(index: Int)

    suspend fun send(payload: Event.User)
}

suspend fun ConversationUserView.sendMessage(content: List<Part>) =
    send(Message(content = content))

suspend fun ConversationUserView.sendToolUseApproval(approvals: Map<Call, Boolean>) =
    send(Approval(approvals = approvals.toList()))
