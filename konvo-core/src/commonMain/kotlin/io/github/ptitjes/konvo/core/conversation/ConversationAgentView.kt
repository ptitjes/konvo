package io.github.ptitjes.konvo.core.conversation

import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.JsonElement

/**
 * Provides a view of the conversation for AI agents.
 * This interface allows agents to receive user events and send assistant events.
 */
interface ConversationAgentView {
    /**
     * Flow of user events from the conversation.
     */
    val events: SharedFlow<ConversationEvent>

    suspend fun sendProcessing()
    suspend fun sendMessage(content: String)
    suspend fun sendToolUseVetting(calls: List<VetoableToolCall>)
    suspend fun sendToolUseResult(
        tool: String,
        arguments: Map<String, JsonElement>,
        result: ToolCallResult,
    )
}
