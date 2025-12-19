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
    conversationManager: ConversationManager,
    private val conversationId: String,
) : ViewModel() {
    private val liveConversation = conversationManager.getConversation(conversationId)
    private val conversationUserView = liveConversation.newUserView()

    private val _state = MutableStateFlow<ConversationViewState>(ConversationViewState.Loading)
    val state: StateFlow<ConversationViewState> = _state

    init {
        println("Initializing ConversationViewModel(${this.conversationId})")
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

    override fun onCleared() {
        super.onCleared()
        println("Cleared ConversationViewModel(${this.conversationId})")
    }

    private fun Event.isViewItem(): Boolean =
        payload !is Event.AssistantProcessing && payload !is Event.ToolUseApproval

    private suspend fun Event.toEventViewState(): EventViewState = when (val details = this.payload) {
        is Event.Message -> {
            val content = details.content.filterIsInstance<ContentPart.Text>().joinToString("\n") { it.text }
            if (sender is Participant.User) {
                EventViewState.UserMessage(
                    event = this,
                    details = details,
                    markdownState = parseMarkdown(content),
                )
            } else {
                EventViewState.AssistantMessage(
                    event = this,
                    details = details,
                    markdownState = parseMarkdown(content),
                )
            }
        }

        is Event.ToolUseVetting -> EventViewState.ToolUseVetting(this, details)
        is Event.ToolUseNotification -> EventViewState.ToolUseNotification(this, details)
        else -> error("Not a view item: $this")
    }

    /**
     * Send a user message to the conversation.
     *
     * @param content The message content to send
     * @param attachments The attachments to send
     */
    fun sendUserMessage(
        content: String,
        attachments: List<Attachment>,
    ) {
        if (content.isBlank()) error("Invalid blank message")

        viewModelScope.launch {
            conversationUserView.sendMessage(
                content = listOf(ContentPart.Text(content)) + attachments.map { attachment ->
                    when (attachment.type) {
                        Attachment.Type.Image -> ContentPart.Image(attachment.mimeType, attachment.name, attachment)
                        Attachment.Type.Video -> ContentPart.Video(attachment.mimeType, attachment.name, attachment)
                        Attachment.Type.Audio -> ContentPart.Audio(attachment.mimeType, attachment.name, attachment)
                        Attachment.Type.Document -> ContentPart.File(attachment.mimeType, attachment.name, attachment)
                    }
                },
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
        override val event: Event,
        val details: Event.Message,
        val markdownState: MarkdownViewState,
    ) : EventViewState

    data class AssistantMessage(
        override val event: Event,
        val details: Event.Message,
        val markdownState: MarkdownViewState,
    ) : EventViewState

    data class ToolUseVetting(
        override val event: Event,
        val details: Event.ToolUseVetting,
    ) : EventViewState

    data class ToolUseNotification(
        override val event: Event,
        val details: Event.ToolUseNotification,
    ) : EventViewState
}

private suspend fun parseMarkdown(content: String): MarkdownViewState =
    parseMarkdownFlow(content).first { it is MarkdownViewState.Success || it is MarkdownViewState.Error }
