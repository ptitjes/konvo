package io.github.ptitjes.konvo.core.conversation.model

import kotlin.time.*

@OptIn(ExperimentalTime::class)
sealed interface Event {

    val id: String
    val timestamp: Instant
    val source: Participant

    interface UserEvent : Event
    interface AssistantEvent : Event

    class UserMessage(
        override val id: String,
        override val timestamp: Instant,
        override val source: Participant,
        val content: String,
        val attachments: List<Attachment>,
    ) : UserEvent {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || other::class != this::class) return false
            other as UserMessage
            return id == other.id
        }

        override fun hashCode(): Int = id.hashCode()

        override fun toString(): String =
            "UserMessage(id=$id, timestamp=$timestamp, source=$source, content=$content, attachments=$attachments)"
    }

    class ToolUseApproval(
        override val id: String,
        override val timestamp: Instant,
        override val source: Participant,
        val vetting: ToolUseVetting,
        val approvals: Map<ToolCall, Boolean>,
    ) : UserEvent {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || other::class != this::class) return false
            other as ToolUseApproval
            return id == other.id
        }

        override fun hashCode(): Int = id.hashCode()

        override fun toString(): String =
            "ToolUseApproval(id=$id, timestamp=$timestamp, source=$source, vetting=$vetting, approvals=$approvals)"
    }

    class AssistantProcessing(
        override val id: String,
        override val timestamp: Instant,
        override val source: Participant,
        val isProcessing: Boolean,
    ) : AssistantEvent {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || other::class != this::class) return false
            other as AssistantProcessing
            return id == other.id
        }

        override fun hashCode(): Int = id.hashCode()

        override fun toString(): String =
            "AssistantProcessing(id=$id, timestamp=$timestamp, source=$source, isProcessing=$isProcessing)"
    }

    class AssistantMessage(
        override val id: String,
        override val timestamp: Instant,
        override val source: Participant,
        val content: String,
    ) : AssistantEvent {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || other::class != this::class) return false
            other as AssistantMessage
            return id == other.id
        }

        override fun hashCode(): Int = id.hashCode()

        override fun toString(): String =
            "AssistantMessage(id=$id, timestamp=$timestamp, source=$source, content=$content)"
    }

    class ToolUseVetting(
        override val id: String,
        override val timestamp: Instant,
        override val source: Participant,
        val calls: List<ToolCall>,
    ) : AssistantEvent {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || other::class != this::class) return false
            other as ToolUseVetting
            return id == other.id
        }

        override fun hashCode(): Int = id.hashCode()

        override fun toString(): String =
            "ToolUseVetting(id=$id, timestamp=$timestamp, source=$source, calls=$calls)"
    }

    class ToolUseNotification(
        override val id: String,
        override val timestamp: Instant,
        override val source: Participant,
        val call: ToolCall,
        val result: ToolCallResult,
    ) : AssistantEvent {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || other::class != this::class) return false
            other as ToolUseNotification
            return id == other.id
        }

        override fun hashCode(): Int = id.hashCode()

        override fun toString(): String =
            "ToolUseNotification(id=$id, timestamp=$timestamp, source=$source, call=$call, result=$result)"
    }
}
