package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.lifecycle.*
import com.mikepenz.markdown.model.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.*
import com.mikepenz.markdown.model.State as MarkdownViewState

/**
 * ViewModel for the conversation UI.
 *
 * This class encapsulates:
 * - Listening to events from the ConversationUiView
 * - Maintaining the conversation entries
 * - Adding messages from the user and sending them to the ConversationUiView
 *
 * @param conversationUserView The view of the conversation to interact with
 */
@OptIn(ExperimentalTime::class, FlowPreview::class)
class ConversationViewModel(
    conversationsManager: ConversationsManager,
    initialConversation: ConversationDigest,
) : ViewModel() {
    private val liveConversation = conversationsManager.getConversation(initialConversation.id)
    private val conversationUserView = liveConversation.newUserView()

    private val _state = MutableStateFlow<ConversationViewState>(ConversationViewState.Loading)
    val state: StateFlow<ConversationViewState> = _state

    init {
        viewModelScope.launch {
            launch {
                var previousTranscript: List<Event> = emptyList()
                var computedItems: List<EventViewState> = emptyList()

                conversationUserView.state.collect { state ->
                    if (state is ConversationState.Loading) return@collect
                    state as ConversationState.Loaded

                    val transcript = state.transcript
                    if (transcript != previousTranscript) {
                        computedItems = transcript.filter { it.isViewItem() }.map { it.toEventViewState() }
                        previousTranscript = transcript
                    }

                    _state.value = ConversationViewState.Loaded(
                        conversation = state.digest,
                        items = computedItems,
                        isProcessing = state.processing,
                    )
                }
            }
        }
    }

    private fun Event.isViewItem(): Boolean =
        this !is Event.AssistantProcessing && this !is Event.ToolUseApproval

    private suspend fun Event.toEventViewState(): EventViewState = when (this) {
        is Event.UserMessage -> EventViewState.UserMessage(
            event = this,
            markdownState = parseMarkdown(content),
        )

        is Event.AssistantMessage -> EventViewState.AssistantMessage(
            event = this,
            markdownState = parseMarkdown(content),
        )

        is Event.ToolUseVetting -> EventViewState.ToolUseVetting(this)
        is Event.ToolUseNotification -> EventViewState.ToolUseNotification(this)
        else -> error("Not a view item: $this")
    }

    /**
     * Send a user message to the conversation.
     *
     * @param message The message to send
     */
    fun sendUserMessage(
        content: String,
        attachments: List<Attachment>,
    ) {
        if (content.isBlank()) error("Invalid blank message")

        viewModelScope.launch {
            conversationUserView.sendMessage(
                content = content,
                attachments = attachments,
            )
        }
    }

    /** Update last read message index, clamped to current items. */
    fun updateLastReadMessageIndex(index: Int) {
        viewModelScope.launch {
            conversationUserView.updateLastReadMessageIndex(index)
        }
    }

    /**
     * Update the conversation title.
     *
     * @param newTitle The new title to set on the conversation
     */
    fun updateTitle(newTitle: String) {
        viewModelScope.launch {
            conversationUserView.updateTitle(newTitle)
        }
    }
}

sealed interface ConversationViewState {
    data object Loading : ConversationViewState
    data class Loaded(
        val conversation: ConversationDigest,
        val items: List<EventViewState>,
        val isProcessing: Boolean,
    ) : ConversationViewState
}

sealed interface EventViewState {
    val event: Event
    val id: String get() = event.id

    data class UserMessage(
        override val event: Event.UserMessage,
        val markdownState: MarkdownViewState,
    ) : EventViewState

    data class AssistantMessage(
        override val event: Event.AssistantMessage,
        val markdownState: MarkdownViewState,
    ) : EventViewState

    data class ToolUseVetting(
        override val event: Event.ToolUseVetting,
    ) : EventViewState

    data class ToolUseNotification(
        override val event: Event.ToolUseNotification,
    ) : EventViewState
}

private suspend fun parseMarkdown(content: String): MarkdownViewState =
    parseMarkdownFlow(content).first { it is MarkdownViewState.Success || it is MarkdownViewState.Error }
