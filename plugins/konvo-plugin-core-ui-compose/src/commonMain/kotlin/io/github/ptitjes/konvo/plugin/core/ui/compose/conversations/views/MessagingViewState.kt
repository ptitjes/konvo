package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.views

import com.mikepenz.markdown.model.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.events.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.spi.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.spi.ConversationViewStates.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.utils.*
import kotlin.time.*

sealed interface MessagingViewState : ConversationViewState.Item {

    data class UserMessage(
        override val id: Any,
        override val timestamp: Instant,
        val details: Messaging.Message,
        val markdownState: State,
    ) : MessagingViewState

    data class AssistantMessage(
        override val id: Any,
        override val timestamp: Instant,
        val details: Messaging.Message,
        val markdownState: State,
    ) : MessagingViewState

    companion object : Contribution {
        override fun ContributionScope.contribute() {
            ConversationViewState.Items {
                onEvent<Messaging.Message> { event ->
                    val content = event.payload.content
                        .filterIsInstance<Messaging.Part.Text>()
                        .joinToString("\n") { it.text }

                    append {
                        if (event.sender is Participant.User) {
                            UserMessage(
                                id = event.id,
                                timestamp = event.timestamp,
                                details = event.payload,
                                markdownState = parseMarkdown(content),
                            )
                        } else {
                            AssistantMessage(
                                id = event.id,
                                timestamp = event.timestamp,
                                details = event.payload,
                                markdownState = parseMarkdown(content),
                            )
                        }
                    }
                }
            }
        }
    }
}
