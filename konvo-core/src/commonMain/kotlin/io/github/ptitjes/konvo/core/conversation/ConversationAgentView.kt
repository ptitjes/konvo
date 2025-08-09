package io.github.ptitjes.konvo.core.conversation

/**
 * Provides a view of the conversation for AI agents.
 * This interface allows agents to send assistant events into a conversation.
 */
interface ConversationAgentView {
    /**
     * Reference to the parent conversation.
     */
    val conversation: ActiveConversation

    suspend fun sendProcessing()

    suspend fun sendMessage(content: String)

    suspend fun sendToolUseVetting(
        calls: List<ToolCall>,
    ): ConversationEvent.AssistantToolUseVetting

    suspend fun sendToolUseResult(
        call: ToolCall,
        result: ToolCallResult,
    )
}
