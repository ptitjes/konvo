package io.github.ptitjes.konvo.core.conversation

import kotlinx.serialization.json.*
import kotlin.time.*

/**
 * Base class for uniquely identified entities: equals/hashCode are based on id only.
 */
abstract class Unique(
    open val id: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other::class != this::class) return false
        other as Unique
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

@OptIn(ExperimentalTime::class)
sealed class ConversationEvent(
    override val id: String,
    open val timestamp: Instant,
    open val source: ConversationMember,
) : Unique(id) {

    // Marker interfaces (do not extend ConversationEvent to allow flexible use)
    interface UserEvent
    interface AssistantEvent

    // User events
    class UserMessage(
        id: String,
        override val timestamp: Instant,
        override val source: ConversationMember,
        val content: String,
        val attachments: List<Attachment>,
    ) : ConversationEvent(id, timestamp, source), UserEvent {
        override fun toString(): String =
            "UserMessage(id=$id, timestamp=$timestamp, source=$source, content=$content, attachments=$attachments)"
    }

    class ToolUseApproval(
        id: String,
        override val timestamp: Instant,
        override val source: ConversationMember,
        val vetting: AssistantToolUseVetting,
        val approvals: Map<ToolCall, Boolean>,
    ) : ConversationEvent(id, timestamp, source), UserEvent {
        override fun toString(): String =
            "ToolUseApproval(id=$id, timestamp=$timestamp, source=$source, vetting=${vetting.id}, approvals=$approvals)"
    }

    // Assistant events
    class AssistantProcessing(
        id: String,
        override val timestamp: Instant,
        override val source: ConversationMember,
    ) : ConversationEvent(id, timestamp, source), AssistantEvent {
        override fun toString(): String =
            "AssistantProcessing(id=$id, timestamp=$timestamp, source=$source)"
    }

    class AssistantMessage(
        id: String,
        override val timestamp: Instant,
        override val source: ConversationMember,
        val content: String,
    ) : ConversationEvent(id, timestamp, source), AssistantEvent {
        override fun toString(): String =
            "AssistantMessage(id=$id, timestamp=$timestamp, source=$source, content=$content)"
    }

    class AssistantToolUseVetting(
        id: String,
        override val timestamp: Instant,
        override val source: ConversationMember,
        val calls: List<ToolCall>,
    ) : ConversationEvent(id, timestamp, source), AssistantEvent {
        override fun toString(): String =
            "AssistantToolUseVetting(id=$id, timestamp=$timestamp, source=$source, calls=$calls)"
    }

    class AssistantToolUseResult(
        id: String,
        override val timestamp: Instant,
        override val source: ConversationMember,
        val call: ToolCall,
        val result: ToolCallResult,
    ) : ConversationEvent(id, timestamp, source), AssistantEvent {
        override fun toString(): String =
            "AssistantToolUseResult(id=$id, timestamp=$timestamp, source=$source, call=$call, result=$result)"
    }
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

class ToolCall(
    override val id: String,
    val tool: String,
    val arguments: Map<String, JsonElement>,
) : Unique(id) {
    override fun toString(): String =
        "VetoableToolCall(id=$id, tool=$tool, arguments=$arguments)"
}

sealed interface ToolCallResult {
    data class Success(val text: String) : ToolCallResult
    data class ExecutionFailure(val reason: String) : ToolCallResult
}
