@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
package io.github.ptitjes.konvo.core.conversation

import kotlinx.serialization.json.*
import kotlin.time.*
import kotlin.uuid.*

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
sealed interface ConversationEvent {
    val id: Uuid
    val timestamp: Instant
    val source: ConversationMember

    // Marker interfaces
    interface UserEvent : ConversationEvent
    interface AssistantEvent : ConversationEvent

    // User events
    data class UserMessage(
        override val id: Uuid,
        override val timestamp: Instant,
        override val source: ConversationMember,
        val content: String,
        val attachments: List<Attachment>,
    ) : UserEvent

    // Assistant events
    data class AssistantProcessing(
        override val id: Uuid,
        override val timestamp: Instant,
        override val source: ConversationMember,
    ) : AssistantEvent

    data class AssistantMessage(
        override val id: Uuid,
        override val timestamp: Instant,
        override val source: ConversationMember,
        val content: String,
    ) : AssistantEvent

    data class AssistantToolUseVetting(
        override val id: Uuid,
        override val timestamp: Instant,
        override val source: ConversationMember,
        val calls: List<VetoableToolCall>,
    ) : AssistantEvent

    data class AssistantToolUseResult(
        override val id: Uuid,
        override val timestamp: Instant,
        override val source: ConversationMember,
        val tool: String,
        val arguments: Map<String, JsonElement>,
        val result: ToolCallResult,
    ) : AssistantEvent
}

data class Attachment(
    val type: Type,
    val url: String,
    val name: String,
    val mimeType: String,
) {
    enum class Type {
        Audio, Image, Video, Document,
    }
}

interface VetoableToolCall {
    val tool: String
    val arguments: Map<String, JsonElement>

    fun allow()
    fun reject()
}

sealed interface ToolCallResult {
    data class Success(val text: String) : ToolCallResult
    data class ExecutionFailure(val reason: String) : ToolCallResult
}
