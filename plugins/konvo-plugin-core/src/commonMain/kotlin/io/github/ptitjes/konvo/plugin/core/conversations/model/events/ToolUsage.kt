package io.github.ptitjes.konvo.plugin.core.conversations.model.events

import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
sealed interface ToolUsage : Action.Payload {

    @Serializable
    @SerialName("ToolUsage#Vetting")
    data class Vetting(
        val calls: List<Call>,
    ) : ToolUsage, Action.Agent

    @Serializable
    @SerialName("ToolUsage#Approval")
    data class Approval(
        val approvals: List<Pair<Call, Boolean>>,
    ) : ToolUsage, Action.User

    @Serializable
    @SerialName("ToolUsage#Notification")
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

    companion object {
        val VettingProtocol = InteractionProtocol(
            id = "ToolUsage#Vetting",
            awaitsInput = true,
            hidesParent = false,
            reactsTo = setOf(Approval::class),
        )
    }
}
