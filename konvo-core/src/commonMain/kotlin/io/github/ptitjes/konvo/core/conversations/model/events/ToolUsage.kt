package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.serialization.json.*

sealed interface ToolUsage : Event.Payload {

    data class Vetting(
        val calls: List<Call>,
    ) : ToolUsage, Event.Agent

    data class Approval(
        val approvals: Map<Call, Boolean>,
    ) : ToolUsage, Event.User

    data class Notification(
        val call: Call,
        val result: CallResult,
    ) : ToolUsage, Event.Agent

    data class Call(
        val id: String,
        val tool: String,
        val arguments: Map<String, JsonElement>,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || other::class != this::class) return false
            other as Call
            return id == other.id
        }

        override fun hashCode(): Int = id.hashCode()
    }

    sealed interface CallResult {
        data class Success(val text: String) : CallResult
        data class ExecutionFailure(val reason: String) : CallResult
    }
}
