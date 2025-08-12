package io.github.ptitjes.konvo.core.conversation

import io.github.ptitjes.konvo.core.conversation.model.*
import kotlinx.coroutines.flow.*

/**
 * Provides a view of the conversation for AI agents.
 * This interface allows agents to send assistant events into a conversation.
 */
interface ConversationAgentView {
    /**
     * Reference to the parent conversation.
     */
    val conversation: LiveConversation

    val transcript: Transcript
    
    val events: SharedFlow<Event>

    suspend fun sendProcessing(isProcessing: Boolean)

    suspend fun sendMessage(content: String)

    suspend fun sendToolUseVetting(
        calls: List<ToolCall>,
    ): Event.ToolUseVetting

    suspend fun sendToolUseResult(
        call: ToolCall,
        result: ToolCallResult,
    )
}
