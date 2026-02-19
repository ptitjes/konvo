package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.conversations.model.Event
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

internal const val PLUGIN_ID = "konvo:io.github.ptitjes.konvo.core"

@Serializable
sealed interface AgentProcessing : Event.Payload {

    @Serializable
    @SerialName("$PREFIX#Start")
    data object Start : AgentProcessing, Event.Agent

    @Serializable
    @SerialName("agent-processing-cancellation")
    data object Cancellation : AgentProcessing, Event.User

    @Serializable
    @SerialName("agent-processing-failure")
    data class Failure(val reason: String) : AgentProcessing, Event.Agent

    @Serializable
    @SerialName("agent-processing-completion")
    data object Completion : AgentProcessing, Event.Agent

    companion object {
        private const val PREFIX = "$PLUGIN_ID/AgentProcessing"

        val TurnBased = InteractionProtocol(
            id = "$PREFIX#TurnBased",
            awaitsInput = true,
            hidesParent = true,
            inputEvents = setOf(Cancellation::class),
        )
    }
}

data class InteractionProtocol(
    val id: String,
    val awaitsInput: Boolean,
    val hidesParent: Boolean,
    val inputEvents: Set<KClass<out Event.Payload>>,
    // FIXME will the conversation helper agent need more?
)
