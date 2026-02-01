package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.*
import io.github.ptitjes.konvo.frontend.compose.conversations.views.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.*

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

    val conversation: ConversationUserView get() = conversationUserView

    private val _state = MutableStateFlow<ConversationViewState>(ConversationViewState.Loading)
    val state: StateFlow<ConversationViewState> = _state

    init {
        println("Initializing ConversationViewModel(${this.conversationId})")
        viewModelScope.launch {
            launch {
                var previousTranscript: List<Event<*>>? = null

                conversationUserView.state.collect { state ->
                    when (state) {
                        is ConversationState.Loading -> {}
                        is ConversationState.Loaded -> {
                            val transcript = state.transcript
                            if (transcript != previousTranscript) {
                                val initial = ConversationViewState.Loaded()
                                    .copy(slot = ConversationViewState.Digest, value = state.digest)

                                val stateUpdater = ConversationViewStateMaintainer(initial)
                                stateUpdater.setupCoreContributors()
                                stateUpdater.handleTranscript(transcript)
                                val finalState = stateUpdater.state

                                previousTranscript = transcript
                                _state.value = finalState
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("Cleared ConversationViewModel(${this.conversationId})")
    }

    /**
     * Send a user message to the conversation.
     *
     * @param content The message content to send
     * @param attachments The attachments to send
     */
    fun sendUserMessage(
        content: String,
        attachments: List<Messaging.Attachment>,
    ) {
        if (content.isBlank()) error("Invalid blank message")

        viewModelScope.launch {
            conversationUserView.sendMessage(
                content = listOf(Messaging.Part.Text(content)) + attachments.map { attachment ->
                    when (attachment.type) {
                        Messaging.Attachment.Type.Image -> Messaging.Part.Image(
                            attachment.mimeType,
                            attachment.name,
                            attachment
                        )

                        Messaging.Attachment.Type.Video -> Messaging.Part.Video(
                            attachment.mimeType,
                            attachment.name,
                            attachment
                        )

                        Messaging.Attachment.Type.Audio -> Messaging.Part.Audio(
                            attachment.mimeType,
                            attachment.name,
                            attachment
                        )

                        Messaging.Attachment.Type.Document -> Messaging.Part.File(
                            attachment.mimeType,
                            attachment.name,
                            attachment
                        )
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

fun ConversationViewStateMaintainer.setupCoreContributors() {
    contributeViewStates(AgentViewState)
    contributeViewStates(MessagingViewState)
    contributeViewStates(ToolUsageViewState)
}
