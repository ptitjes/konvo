package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.views

import io.github.ptitjes.konvo.plugin.core.conversations.model.events.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.spi.*
import kotlin.time.*

data class PresenceViewState(
    val present: Boolean = false,
    val lastViewTimestamp: Instant? = null,
) {
    companion object : ConversationViewStates.Contribution {
        override fun ConversationViewStates.ContributionScope.contribute() {
            ConversationViewState.Presence {
                onEvent<Presence.Joining> { joiningEvent ->
                    put(key = joiningEvent.sender, value = { PresenceViewState(present = true) }) {
                        onEvent<Presence.ViewNotification> { state, notificationEvent ->
                            if (notificationEvent.sender != joiningEvent.sender) return@onEvent state

                            val presence = state ?: PresenceViewState()
                            presence.copy(lastViewTimestamp = notificationEvent.payload.upToTimestamp)
                        }
                        onEvent<Presence.Leaving> { state, leavingEvent ->
                            if (leavingEvent.sender != joiningEvent.sender) return@onEvent state

                            freeze()

                            val presence = state ?: PresenceViewState()
                            presence.copy(present = false)
                        }
                    }
                }
            }
        }
    }
}
