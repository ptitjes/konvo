package io.github.ptitjes.konvo.frontend.compose.viewmodels

import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.core.conversation.model.*
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
    private val conversationUserView: ConversationUserView,
) : ViewModel() {

    private val _viewItems = MutableStateFlow(initialItems())
    val viewItems: StateFlow<List<Event>> = _viewItems

    private fun initialItems(): List<Event> =
        conversationUserView.transcript.events.filter {
            it.isViewItem()
        }

    private fun Event.isViewItem(): Boolean =
        this !is Event.AssistantProcessing && this !is Event.ToolUseApproval

    init {
        viewModelScope.launch {
            conversationUserView.events.filter {
                it.isViewItem()
            }.collect { event ->
                _viewItems.update { it + event }
            }
        }
    }

    val assistantIsProcessing =
        conversationUserView.events
            .filterIsInstance<Event.AssistantProcessing>()
            .map { it.isProcessing }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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
