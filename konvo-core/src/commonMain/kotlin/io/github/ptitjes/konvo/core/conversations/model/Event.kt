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

    interface Agent : Payload
    interface User : Payload

    data class Message(
        val content: List<ContentPart>,
    ) : Agent, User

    data class AssistantProcessing(
        val isProcessing: Boolean,
    ) : Agent

    sealed interface ToolUsage : Payload

    data class ToolUseVetting(
        val calls: List<ToolCall>,
    ) : ToolUsage, Agent

    data class ToolUseApproval(
        val approvals: Map<ToolCall, Boolean>,
    ) : ToolUsage, User

    data class ToolUseNotification(
        val call: ToolCall,
        val result: ToolCallResult,
    ) : ToolUsage, Agent
}
