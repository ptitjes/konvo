package io.github.ptitjes.konvo.frontend.compose.viewmodels

import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

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
class ConversationViewModel(
    liveConversationsManager: LiveConversationsManager,
    conversationRepository: ConversationRepository,
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

    init {
        viewModelScope.launch {
            liveConversation.awaitLoaded()

            val initialItems = conversationUserView.transcript.events.filter { it.isViewItem() }

            _state.emit(
                ConversationViewState.Loaded(
                    items = initialItems,
                    isProcessing = false,
                )
            )

            launch {
                conversationUserView.events.collect { event ->
                    _state.update { previousState ->
                        check(previousState is ConversationViewState.Loaded) { "Invalid state" }

                        when {
                            event.isViewItem() -> previousState.copy(
                                items = previousState.items + event,
                            )

                            event is Event.AssistantProcessing -> previousState.copy(
                                isProcessing = event.isProcessing,
                            )

                            else -> previousState
                        }
                    }
                }
            }
        }
    }

    private fun Event.isViewItem(): Boolean =
        this !is Event.AssistantProcessing && this !is Event.ToolUseApproval

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
}

sealed interface ConversationViewState {
    data object Loading : ConversationViewState
    data class Loaded(
        val items: List<Event>,
        val isProcessing: Boolean,
    ) : ConversationViewState
}
