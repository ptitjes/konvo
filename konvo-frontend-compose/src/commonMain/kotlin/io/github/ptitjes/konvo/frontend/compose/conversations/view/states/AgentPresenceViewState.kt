package io.github.ptitjes.konvo.frontend.compose.conversations.view.states

import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.dsl.ConversationViewStateContribution.*

sealed interface AgentPresenceViewState {
    companion object : Contribution {
        override fun ContributionsScope.contribute() {
            onEvent<AgentPresence.Processing> { event ->
                set(
                    ConversationViewState.AgentState,
                    event.payload.isProcessing,
                )
            }
        }
    }
}
