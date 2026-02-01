package io.github.ptitjes.konvo.frontend.compose.conversations.views

import com.mikepenz.markdown.model.*
import com.mikepenz.markdown.model.State
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.ConversationViewState
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.ConversationViewStates.*
import kotlinx.coroutines.flow.*
import com.mikepenz.markdown.model.State as MarkdownViewState

sealed interface MessagingViewState : ConversationViewState.Item {

    data class UserMessage(
        override val id: Any,
        val details: Messaging.Message,
        val markdownState: State,
    ) : MessagingViewState

    data class AssistantMessage(
        override val id: Any,
        val details: Messaging.Message,
        val markdownState: State,
    ) : MessagingViewState

    companion object : Contribution {
        override fun CreateScope.contribute() {
            onEvent<Messaging.Message> { event ->
                val content =
                    event.payload.content.filterIsInstance<Messaging.Part.Text>().joinToString("\n") { it.text }

                append(
                    ConversationViewState.Items,
                    if (event.sender is Participant.User) {
                        UserMessage(
                            id = event.id,
                            details = event.payload,
                            markdownState = parseMarkdown(content),
                        )
                    } else {
                        AssistantMessage(
                            id = event.id,
                            details = event.payload,
                            markdownState = parseMarkdown(content),
                        )
                    }
                )
            }
        }
    }
}

private suspend fun parseMarkdown(content: String): MarkdownViewState =
    parseMarkdownFlow(content).first { it is MarkdownViewState.Success || it is MarkdownViewState.Error }
