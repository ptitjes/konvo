package io.github.ptitjes.konvo.frontend.compose.viewmodels

import androidx.compose.runtime.*
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
    // The list of conversation entries, exposed as an immutable list
    private val _conversationEntries = mutableStateListOf<ConversationUiEntry>()
    val conversationEntries: List<ConversationUiEntry> = _conversationEntries

    val assistantIsProcessing = conversationUiView.events
        .map { it is ConversationEvent.AssistantProcessing || it is ConversationEvent.AssistantToolUseResult }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        // Start collecting assistant events
        viewModelScope.launch {
            conversationUiView.events.collect { event ->
                if (event.source is ConversationMember.Agent && event is ConversationEvent.AssistantEvent) {
                    processAssistantEvent(event)
                }
            }
        }
    }

    /**
     * Process an assistant event and update the conversation entries accordingly.
     */
    private fun processAssistantEvent(event: ConversationEvent.AssistantEvent) {
        when (event) {
            is ConversationEvent.AssistantMessage -> {
                _conversationEntries.add(ConversationUiEntry.Assistant(event.content))
            }

            is ConversationEvent.AssistantProcessing -> {
                println(event)
                // Could show a loading indicator here
            }

            is ConversationEvent.AssistantToolUseVetting, is ConversationEvent.AssistantToolUseResult -> {
                println(event)
                // Handle tool-related events if needed
            }
        }
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

        // Add the user message to conversation entries
        _conversationEntries.add(
            ConversationUiEntry.UserMessage(
                content = content,
                attachments = attachments,
            )
        )

        // Send the message to the conversation UI view
        viewModelScope.launch {
            conversationUiView.sendMessage(
                content = content,
                attachments = attachments,
            )
        }
    }
}

/**
 * Represents an entry in the conversation.
 */
sealed interface ConversationUiEntry {
    val content: String

    /**
     * A message from the user.
     */
    data class UserMessage(
        override val content: String,
        val attachments: List<Attachment>,
    ) : ConversationUiEntry

    /**
     * A message from the assistant.
     */
    data class Assistant(override val content: String) : ConversationUiEntry
}
