package io.github.ptitjes.konvo.frontend.compose.conversations.view.states

import io.github.ptitjes.konvo.core.conversations.model.events.AgentPresence.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.dsl.ConversationViewStateContribution.*

sealed interface AgentPresenceViewState {

    data class Presence(
        val status: Status,
    ) {
        sealed interface Status {
            data object Joined : Status
            data class Available(val messagingCapabilities: MessagingCapabilities) : Status
            data object Processing : Status
        }
    }

    companion object : Contribution {
        override fun ContributionsScope.contribute() {
            onEvent<Joining> { event ->
                set<Presence?, Presence?>(
                    ConversationViewState.AgentState,
                    Presence(status = Presence.Status.Joined),
                ) {
                    onEvent<Available> { _, event ->
                        Presence(status = Presence.Status.Available(event.payload.messagingCapabilities))
                    }
                    onEvent<Processing> { _, _ -> Presence(status = Presence.Status.Processing) }
                    onEvent<Leaving> { _, _ -> null }
                }
            }
        }
    }
}
