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

    val conversation: ConversationUserView get() = conversationUserView

    private val _state = MutableStateFlow<ConversationViewState>(ConversationViewState.Loading)
    val state: StateFlow<ConversationViewState> = _state

    init {
        println("Initializing ConversationViewModel(${this.conversationId})")
        viewModelScope.launch {
            launch {
                var previousTranscript: List<Event>? = null

                conversationUserView.state.collect { state ->
                    when (state) {
                        is ConversationState.Loading -> {}
                        is ConversationState.Loaded -> {
                            val transcript = state.transcript
                            if (transcript != previousTranscript) {
                                val initial = ConversationViewState.Loaded(
                                    conversation = state.digest,
                                    items = emptyList(),
                                    isProcessing = false,
                                )

                                val finalState = transcript.fold(initial) { state, event ->
                                    updateState(state, event)
                                }

                                previousTranscript = transcript
                                _state.value = finalState
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun updateState(state: ConversationViewState.Loaded, event: Event): ConversationViewState.Loaded {
        return when (val payload = event.payload) {
            is Event.AssistantProcessing -> state.copy(isProcessing = payload.isProcessing)

            is Event.Message -> {
                val content = payload.content.filterIsInstance<ContentPart.Text>().joinToString("\n") { it.text }
                val message = if (event.sender is Participant.User) {
                    ItemViewState.UserMessage(
                        id = event.id,
                        details = payload,
                        markdownState = parseMarkdown(content),
                    )
                } else {
                    ItemViewState.AssistantMessage(
                        id = event.id,
                        details = payload,
                        markdownState = parseMarkdown(content),
                    )
                }
                state.copy(items = state.items + message)
            }

            is Event.ToolUseVetting -> {
                state.copy(
                    items = state.items + ItemViewState.ToolUseVetting(
                        id = event.id,
                        approvals = payload.calls.associateWith { ItemViewState.ToolUseVetting.ApprovalStatus.Pending },
                    ),
                )
            }

            is Event.ToolUseApproval -> {
                val toolUseVettingIndex = state.items.indexOfLast {
                    it is ItemViewState.ToolUseVetting && it.approvals.keys.containsAll(payload.approvals.keys)
                }

                require(toolUseVettingIndex != 0) { "No tool use vetting found for approval" }

                val toolUseVetting = state.items[toolUseVettingIndex] as ItemViewState.ToolUseVetting
                val updatedToolUseVetting = toolUseVetting.copy(
                    approvals = payload.approvals.mapValues { (call, approval) ->
                        when (approval) {
                            true -> ItemViewState.ToolUseVetting.ApprovalStatus.Approved
                            false -> ItemViewState.ToolUseVetting.ApprovalStatus.Denied("Not specified")
                        }
                    }
                )

                state.copy(
                    items = state.items.mapIndexed { index, itemViewState ->
                        if (index == toolUseVettingIndex) updatedToolUseVetting else itemViewState
                    }
                )
            }

            is Event.ToolUseNotification -> {
                state.copy(
                    items = state.items + ItemViewState.ToolUseNotification(
                        id = event.id,
                        call = payload.call,
                        result = payload.result,
                    )
                )
            }

            else -> error("Not a view item: $event")
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
        val items: List<ItemViewState>,
        val isProcessing: Boolean,
    ) : ConversationViewState
}

sealed interface ItemViewState {
    val id: Any

    data class UserMessage(
        override val id: Any,
        val details: Event.Message,
        val markdownState: MarkdownViewState,
    ) : ItemViewState

    data class AssistantMessage(
        override val id: Any,
        val details: Event.Message,
        val markdownState: MarkdownViewState,
    ) : ItemViewState

    data class ToolUseVetting(
        override val id: Any,
        val approvals: Map<ToolCall, ApprovalStatus>,
    ) : ItemViewState {
        sealed interface ApprovalStatus {
            data object Pending : ApprovalStatus
            data object Approved : ApprovalStatus
            data class Denied(val reason: String) : ApprovalStatus
        }
    }

    data class ToolUseNotification(
        override val id: Any,
        val call: ToolCall,
        val result: ToolCallResult,
    ) : ItemViewState
}

private suspend fun parseMarkdown(content: String): MarkdownViewState =
    parseMarkdownFlow(content).first { it is MarkdownViewState.Success || it is MarkdownViewState.Error }
