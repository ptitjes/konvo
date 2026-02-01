package io.github.ptitjes.konvo.frontend.compose.conversations.views

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.ConversationViewState
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.ConversationViewStates.*
import kotlinx.serialization.*

data class AgentViewState(
    val isProcessing: Boolean = false, // later val tasks: List<Task> = emptyList(),
    val messagingCapabilities: MessagingCapabilities? = null,
) {
    @Serializable
    data class MessagingCapabilities(
        val supportedMediaTypes: List<String> = emptyList(),
    )

    companion object : Contribution {
        override fun CreateScope.contribute() {
            onEvent<AgentPresence.Joining> { event ->
                put(
                    ConversationViewState.Agents,
                    event.sender as Participant.Agent,
                    AgentViewState(),
                ) {
                    onEvent<AgentCapabilities.Messaging> { state, event ->
                        state!!.copy(messagingCapabilities = MessagingCapabilities(event.payload.supportedMediaTypes))
                    }
                    onEvent<AgentProcessing.Start> { state, _ -> state!!.copy(isProcessing = true) }
                    onEvent<AgentProcessing.Completion> { state, _ -> state!!.copy(isProcessing = false) }
                    onEvent<AgentPresence.Leaving> { _, _ ->
                        freeze()
                        null
                    }
                }
            }
        }
    }
}
