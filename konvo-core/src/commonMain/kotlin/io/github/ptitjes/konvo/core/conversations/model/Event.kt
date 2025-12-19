package io.github.ptitjes.konvo.core.conversations.model

import kotlin.time.*

data class Event(
    val id: String,
    val timestamp: Instant,
    val sender: Participant,
    val recipients: Set<Participant>? = null,
    val payload: Payload,
) {

    interface Payload

    data class UserMessage(
        val content: String,
        val attachments: List<Attachment>,
    ) : Payload

    data class ToolUseApproval(
        val vetting: ToolUseVetting,
        val approvals: Map<ToolCall, Boolean>,
    ) : Payload

    data class AssistantProcessing(
        val isProcessing: Boolean,
    ) : Payload

    data class AssistantMessage(
        val content: String,
    ) : Payload

    data class ToolUseVetting(
        val calls: List<ToolCall>,
    ) : Payload

    data class ToolUseNotification(
        val call: ToolCall,
        val result: ToolCallResult,
    ) : Payload
}
