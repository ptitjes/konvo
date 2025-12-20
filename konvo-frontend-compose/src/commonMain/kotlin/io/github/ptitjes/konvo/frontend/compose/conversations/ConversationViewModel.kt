package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.lifecycle.*
import com.mikepenz.markdown.model.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.reflect.*
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

                                val stateUpdater = ConversationStateUpdater(initial)
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

suspend fun ConversationStateUpdater.handleTranscript(transcript: List<Event>) {
    for (element in transcript) handleEvent(element)
}

class ConversationStateUpdater(
    initialState: ConversationViewState.Loaded,
) {
    var state = initialState
        private set

    private val registeredStateUpdater =
        mutableMapOf<KClass<out Event.Payload>, MutableList<suspend (ConversationViewState.Loaded, Event, Event.Payload) -> ConversationViewState.Loaded>>()

    inline fun <reified P : Event.Payload> addStateUpdater(
        noinline handler: suspend (ConversationViewState.Loaded, Event, P) -> ConversationViewState.Loaded,
    ): () -> Unit = addStateUpdater(P::class, handler)

    fun <P : Event.Payload> addStateUpdater(
        klass: KClass<out P>,
        handler: suspend (ConversationViewState.Loaded, Event, P) -> ConversationViewState.Loaded,
    ): () -> Unit {
        registeredStateUpdater[klass] = (registeredStateUpdater[klass] ?: mutableListOf()).also {
            @Suppress("UNCHECKED_CAST")
            it += handler as suspend (ConversationViewState.Loaded, Event, Event.Payload) -> ConversationViewState.Loaded
        }
        return { registeredStateUpdater[klass]?.remove(handler) }
    }

    suspend fun handleEvent(event: Event) {
        val payload = event.payload
        val updaters = this@ConversationStateUpdater.registeredStateUpdater[payload::class]?.toList() ?: return
        state = updaters.fold(state) { state, updater -> updater(state, event, payload) }
    }

    @DslMarker
    annotation class EventContributionDslMarker

    @EventContributionDslMarker
    inline fun <reified P : Event.Payload> onEvent(
        crossinline action: suspend ContributionBuilderScope.(Event, P) -> Unit,
    ) {
        addStateUpdater<P> { state, event, payload ->
            val scope = ContributionBuilderScope(this, state)
            scope.action(event, payload)
            scope.state
        }
    }

    @EventContributionDslMarker
    class ContributionBuilderScope(
        private val stateUpdater: ConversationStateUpdater,
        initialState: ConversationViewState.Loaded,
    ) {
        var state: ConversationViewState.Loaded = initialState
            private set

        fun <S : ItemViewState> contributeItem(
            initialViewState: S,
            builder: HandlersBuilderScope<S>.() -> Unit = {},
        ) {
            state = state.copy(items = state.items + initialViewState)

            HandlersBuilderScope(stateUpdater, initialViewState).builder()
        }
    }

    @EventContributionDslMarker
    class HandlersBuilderScope<S : ItemViewState>(
        private val stateUpdater: ConversationStateUpdater,
        private val initialViewState: S,
    ) {
        private val teardowns = mutableListOf<() -> Unit>()

        private val handlerScope = object : HandlerBuilderScope() {
            override fun freezeItem() {
                this@HandlersBuilderScope.teardowns.forEach { it() }
            }
        }

        inline fun <reified Q : Event.Payload> onEvent(
            noinline handler: suspend HandlerBuilderScope.(S, Q) -> S,
        ) = onEvent(Q::class, handler)

        fun <Q : Event.Payload> onEvent(
            klass: KClass<Q>,
            handler: suspend HandlerBuilderScope.(S, Q) -> S,
        ) {
            teardowns += stateUpdater.addStateUpdater(klass) { state, event, payload ->
                val itemViewStateIndex = state.items.indexOfLast { it.id == initialViewState.id }
                require(itemViewStateIndex != -1) { "No view state found for initial view state" }
                val itemViewState = state.items[itemViewStateIndex]

                @Suppress("UNCHECKED_CAST")
                val updatedItemViewState = handlerScope.handler(itemViewState as S, payload)
                state.copy(
                    items = state.items.mapIndexed { index, itemViewState ->
                        if (index == itemViewStateIndex) updatedItemViewState else itemViewState
                    }
                )
            }
        }
    }

    @EventContributionDslMarker
    abstract class HandlerBuilderScope {
        abstract fun freezeItem()
    }

    init {
        addStateUpdater<Event.AssistantProcessing> { state, event, payload ->
            println("Processing state update for AssistantProcessing")
            state.copy(isProcessing = payload.isProcessing)
        }

        onEvent<Event.Message> { event, payload ->
            val content = payload.content.filterIsInstance<ContentPart.Text>().joinToString("\n") { it.text }
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
        onEvent<Event.ToolUseVetting> { event, payload ->
            contributeItem(
                initialViewState = ItemViewState.ToolUseVetting(
                    id = event.id,
                    approvals = payload.calls.associateWith { ItemViewState.ToolUseVetting.ApprovalStatus.Pending },
                ),
            ) {
                onEvent<Event.ToolUseApproval> { state, approvalPayload ->
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
        onEvent<Event.ToolUseNotification> { event, payload ->
            contributeItem(
                ItemViewState.ToolUseNotification(
                    id = event.id,
                    call = payload.call,
                    result = payload.result,
                ),
            )
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
