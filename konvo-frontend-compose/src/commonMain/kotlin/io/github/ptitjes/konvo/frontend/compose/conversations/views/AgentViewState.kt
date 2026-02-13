package io.github.ptitjes.konvo.frontend.compose.conversations.views

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.ConversationViewStates.*

data class AgentViewState(
    val isProcessing: Boolean = false, // later val tasks: List<Task> = emptyList(),
    val messagingCapabilities: MessagingCapabilities? = null,
) {
    data class MessagingCapabilities(
        val supportedMediaTypes: List<String> = emptyList(),
    )

    companion object : Contribution {
        override fun ContributionScope.contribute() {
            ConversationViewState.Agents {
                onEvent<Presence.Joining> { event ->
                    put(key = event.sender as Participant.Agent, initial = AgentViewState()) {
                        onEvent<AgentCapabilities.Messaging> { state, event ->
                            val supportedMediaTypes = event.payload.supportedMediaTypes
                            state!!.copy(messagingCapabilities = MessagingCapabilities(supportedMediaTypes))
                        }
                        onEvent<AgentProcessing.Start> { state, _ ->
                            state!!.copy(isProcessing = true)
                        }
                        onEvent<AgentProcessing.Completion> { state, _ ->
                            state!!.copy(isProcessing = false)
                        }
                        onEvent<Presence.Leaving> { _, _ ->
                            freeze()
                            null
                        }
                    }
                }
            }
        }
    }
}
