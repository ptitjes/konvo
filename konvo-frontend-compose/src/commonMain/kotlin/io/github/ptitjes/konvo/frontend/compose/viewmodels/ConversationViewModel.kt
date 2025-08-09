package io.github.ptitjes.konvo.frontend.compose.viewmodels

import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.conversation.*
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
 * @param conversationUiView The view of the conversation to interact with
 */
class ConversationViewModel(
    private val conversationUiView: ConversationUserView,
) : ViewModel() {

    val events = conversationUiView.conversation.transcript.events

    val assistantIsProcessing =
        conversationUiView.conversation.events
            .map { it is ConversationEvent.AssistantProcessing || it is ConversationEvent.AssistantToolUseResult }
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
            conversationUiView.sendMessage(
                content = content,
                attachments = attachments,
            )
        }
    }
}
