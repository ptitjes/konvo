package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.lifecycle.*
import com.mikepenz.markdown.model.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.storage.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.*
import kotlin.time.Duration.Companion.milliseconds
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
    liveConversationsManager: LiveConversationsManager,
    private val conversationRepository: ConversationRepository,
    initialConversation: Conversation,
) : ViewModel() {
    // Expose a flow of the conversation that updates with repository changes
    val conversation: StateFlow<Conversation> = conversationRepository
        .getConversation(initialConversation.id)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = initialConversation,
        )

    private val liveConversation = liveConversationsManager.getLiveConversation(initialConversation.id)
    private val conversationUserView = liveConversation.newUserView()

    private val _state = MutableStateFlow<ConversationViewState>(ConversationViewState.Loading)
    val state: StateFlow<ConversationViewState> = _state

    // Debounced title changes to avoid hammering the repository while typing
    private val _pendingUpdatedTitle = MutableSharedFlow<String>(extraBufferCapacity = 64)

    init {
        viewModelScope.launch {
            liveConversation.awaitLoaded()

            val initialItems = mutableListOf<EventViewState>()
            for (event in conversationUserView.transcript.events) {
                if (event.isViewItem()) {
                    initialItems += event.toEventViewState()
                }
            }

            _state.emit(
                ConversationViewState.Loaded(
                    items = initialItems,
                    isProcessing = false,
                    lastReadMessageIndex = conversationUserView.lastReadMessageIndex.value,
                )
            )

            launch {
                conversationUserView.lastReadMessageIndex.collect { index ->
                    _state.update { prev ->
                        check(prev is ConversationViewState.Loaded) { "Invalid state" }
                        prev.copy(lastReadMessageIndex = index)
                    }
                }
            }

            launch {
                conversationUserView.events.collect { event ->
                    when {
                        event is Event.AssistantProcessing -> {
                            _state.update { previousState ->
                                check(previousState is ConversationViewState.Loaded) { "Invalid state" }
                                previousState.copy(isProcessing = event.isProcessing)
                            }
                        }

                        event.isViewItem() -> {
                            val eventViewState = event.toEventViewState()

                            _state.update { previousState ->
                                check(previousState is ConversationViewState.Loaded) { "Invalid state" }
                                val prevItemsSize = previousState.items.size
                                val newState = previousState.copy(items = previousState.items + eventViewState)
                                // If it's a user message, mark it (and thus all previous) as read
                                if (event is Event.UserMessage) {
                                    viewModelScope.launch {
                                        conversationUserView.updateLastReadMessageIndex(prevItemsSize)
                                    }
                                }
                                newState
                            }
                        }

                        else -> {}
                    }
                }
            }
        }

        // Collect pending title updates with debounce
        viewModelScope.launch {
            _pendingUpdatedTitle
                .debounce(500.milliseconds)
                .distinctUntilChanged()
                .collect { newTitle ->
                    val current = conversation.value
                    if (current.title != newTitle) {
                        conversationRepository.updateConversation(current.copy(title = newTitle))
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
    @OptIn(ExperimentalTime::class)
    fun updateTitle(newTitle: String) {
        // Emit changes to a debounced flow to avoid persisting on every keystroke
        _pendingUpdatedTitle.tryEmit(newTitle)
    }
}

sealed interface ConversationViewState {
    data object Loading : ConversationViewState
    data class Loaded(
        val items: List<EventViewState>,
        val isProcessing: Boolean,
        val lastReadMessageIndex: Int,
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
