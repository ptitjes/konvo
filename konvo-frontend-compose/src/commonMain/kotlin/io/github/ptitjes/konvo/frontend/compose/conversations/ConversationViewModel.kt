package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.lifecycle.*
import com.mikepenz.markdown.model.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.Attachment
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.Part
import io.github.ptitjes.konvo.frontend.compose.conversations.view.ConversationStateMaintainer
import io.github.ptitjes.konvo.frontend.compose.conversations.view.ConversationViewState
import io.github.ptitjes.konvo.frontend.compose.conversations.view.ItemViewState
import io.github.ptitjes.konvo.frontend.compose.conversations.view.handleTranscript
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

                                val stateUpdater = ConversationStateMaintainer(initial)
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
        attachments: List<Attachment>,
    ) {
        if (content.isBlank()) error("Invalid blank message")

        viewModelScope.launch {
            conversationUserView.sendMessage(
                content = listOf(Part.Text(content)) + attachments.map { attachment ->
                    when (attachment.type) {
                        Attachment.Type.Image -> Part.Image(attachment.mimeType, attachment.name, attachment)
                        Attachment.Type.Video -> Part.Video(attachment.mimeType, attachment.name, attachment)
                        Attachment.Type.Audio -> Part.Audio(attachment.mimeType, attachment.name, attachment)
                        Attachment.Type.Document -> Part.File(attachment.mimeType, attachment.name, attachment)
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

fun ConversationStateMaintainer.setupCoreContributors() {
    addStateUpdater<AgentPresence.Processing> { state, event, payload ->
        println("Processing state update for AssistantProcessing")
        state.copy(isProcessing = payload.isProcessing)
    }

    onEvent<Messaging.Message> { event, payload ->
        val content = payload.content.filterIsInstance<Part.Text>().joinToString("\n") { it.text }
        contributeItem(
            if (event.sender is Participant.User) {
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
        )
    }
    onEvent<ToolUsage.Vetting> { event, payload ->
        contributeItem(
            initialViewState = ItemViewState.ToolUseVetting(
                id = event.id,
                approvals = payload.calls.associateWith { ItemViewState.ToolUseVetting.ApprovalStatus.Pending },
            ),
        ) {
            onEvent<ToolUsage.Approval> { state, approvalPayload ->
                val changedApprovals = approvalPayload.approvals.keys.fold(state.approvals) { acc, key ->
                    val newValue by lazy {
                        val approved = approvalPayload.approvals[key]
                        when (approved) {
                            true -> ItemViewState.ToolUseVetting.ApprovalStatus.Approved
                            false -> ItemViewState.ToolUseVetting.ApprovalStatus.Denied("Not specified")
                            null -> ItemViewState.ToolUseVetting.ApprovalStatus.Pending
                        }
                    }
                    if (key in acc) acc + (key to newValue) else acc
                }

                val done =
                    changedApprovals.values.all { it !is ItemViewState.ToolUseVetting.ApprovalStatus.Pending }

                if (done) freezeItem()

                state.copy(
                    approvals = changedApprovals
                )
            }
        }
    }
    onEvent<ToolUsage.Notification> { event, payload ->
        contributeItem(
            ItemViewState.ToolUseNotification(
                id = event.id,
                call = payload.call,
                result = payload.result,
            ),
        )
    }
}

private suspend fun parseMarkdown(content: String): MarkdownViewState =
    parseMarkdownFlow(content).first { it is MarkdownViewState.Success || it is MarkdownViewState.Error }
