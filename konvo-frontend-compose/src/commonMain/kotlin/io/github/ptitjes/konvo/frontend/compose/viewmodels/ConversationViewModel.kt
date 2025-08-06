package io.github.ptitjes.konvo.frontend.compose.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.frontend.compose.attachments.*
import io.github.vinceglb.filekit.core.*
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
    private val conversationUiView: ConversationUiView,
) : ViewModel() {
    // The list of conversation entries, exposed as an immutable list
    private val _conversationEntries = mutableStateListOf<ConversationEntry>()
    val conversationEntries: List<ConversationEntry> = _conversationEntries

    val assistantIsProcessing = conversationUiView.assistantEvents
        .map { it is AssistantEvent.Processing || it is AssistantEvent.ToolUseResult }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        // Start collecting assistant events
        viewModelScope.launch {
            conversationUiView.assistantEvents.collectLatest { event ->
                processAssistantEvent(event)
            }
        }
    }

    /**
     * Process an assistant event and update the conversation entries accordingly.
     */
    private fun processAssistantEvent(event: AssistantEvent) {
        when (event) {
            is AssistantEvent.Message -> {
                _conversationEntries.add(ConversationEntry.Assistant(event.content))
            }

            is AssistantEvent.Processing -> {
                // Could show a loading indicator here
            }

            is AssistantEvent.ToolUseVetting, is AssistantEvent.ToolUseResult -> {
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
        attachments: List<PlatformFile>,
    ) {
        if (content.isBlank()) error("Invalid blank message")

        // Add the user message to conversation entries
        _conversationEntries.add(
            ConversationEntry.UserMessage(
                content = content,
                attachments = attachments,
            )
        )

        // Send the message to the conversation UI view
        viewModelScope.launch {
            conversationUiView.sendUserEvent(
                UserEvent.Message(
                    content = content,
                    attachments = attachments.map { it.createFileAttachement() },
                )
            )
        }
    }
}

/**
 * Represents an entry in the conversation.
 */
sealed interface ConversationEntry {
    val content: String

    /**
     * A message from the user.
     */
    data class UserMessage(
        override val content: String,
        val attachments: List<PlatformFile>,
    ) : ConversationEntry

    /**
     * A message from the assistant.
     */
    data class Assistant(override val content: String) : ConversationEntry
}
