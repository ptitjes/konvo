package io.github.ptitjes.konvo.core.conversations

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.Part
import io.github.ptitjes.konvo.core.conversations.model.events.ToolUsage.Call
import io.github.ptitjes.konvo.core.conversations.model.events.ToolUsage.CallResult
import kotlinx.coroutines.flow.*

/**
 * Provides a view of the conversation for AI agents.
 * This interface allows agents to send assistant events into a conversation.
 */
interface ConversationAgentView {

    val events: SharedFlow<Event<*>>

    suspend fun sendProcessing(isProcessing: Boolean)

    suspend fun sendMessage(content: List<Part>)

    suspend fun sendToolUseVetting(
        calls: List<Call>,
    )

    suspend fun sendToolUseResult(
        call: Call,
        result: CallResult,
    )

    suspend fun send(payload: Event.Agent)
}
