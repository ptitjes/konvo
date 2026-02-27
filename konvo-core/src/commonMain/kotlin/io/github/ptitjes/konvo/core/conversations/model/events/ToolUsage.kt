package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
sealed interface ToolUsage : Action.Payload {

    @Serializable
    @SerialName("tool-usage-vetting")
    data class Vetting(
        val calls: List<Call>,
    ) : ToolUsage, Action.Agent

    @Serializable
    @SerialName("tool-usage-approval")
    data class Approval(
        val approvals: List<Pair<Call, Boolean>>,
    ) : ToolUsage, Action.User

    @Serializable
    @SerialName("tool-usage-notification")
    data class Notification(
        val call: Call,
        val result: CallResult,
    ) : ToolUsage, Action.Agent

    @Serializable
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

    @Serializable
    sealed interface CallResult {
        @Serializable
        @SerialName("success")
        data class Success(val value: JsonElement?) : CallResult

        @Serializable
        @SerialName("execution-failure")
        data class ExecutionFailure(val reason: String) : CallResult
    }
}
