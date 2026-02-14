package io.github.ptitjes.konvo.frontend.compose.conversations.views

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.*
import kotlin.time.*

data class PreviewViewState(
    val title: String? = null,
    val lastMessageTimestamp: Instant? = null,
    val lastMessagePreview: String? = null,
) {
    companion object : ConversationViewStates.Contribution {
        override fun ConversationViewStates.ContributionScope.contribute() {
            ConversationViewState.Preview {
                onEvent<Metadata.TitleChange> { event ->
                    set { it.copy(title = event.payload.title) }
                }
                onEvent<Messaging.Message> { event ->
                    set {
                        it.copy(
                            lastMessageTimestamp = event.timestamp,
                            lastMessagePreview = event.payload.computeMessagePreview(),
                        )
                    }
                }
            }
        }
    }
}
