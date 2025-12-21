@file:OptIn(ExperimentalTime::class)

package io.github.ptitjes.konvo.core.conversations

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.Part
import io.github.ptitjes.konvo.core.conversations.model.events.ToolUsage.Call
import io.github.ptitjes.konvo.core.conversations.model.events.ToolUsage.CallResult
import io.github.ptitjes.konvo.core.conversations.storage.*
import io.github.ptitjes.konvo.core.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*
import kotlin.time.*
import kotlin.time.Duration.Companion.milliseconds

sealed interface ConversationState {
    data object Loading : ConversationState
    data class Loaded(
        val digest: ConversationDigest,
        val transcript: List<Event>,
        val processing: Boolean,
    ) : ConversationState
}

@OptIn(FlowPreview::class)
class Conversation(
    coroutineContext: CoroutineContext,
    private val conversationId: String,
    private val repository: ConversationRepository,
    private val agentFactory: AgentFactory,
    private val timeProvider: TimeProvider = SystemTimeProvider,
    private val idGenerator: IdGenerator = UuidIdGenerator,
) : AutoCloseable {

    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val job = SupervisorJob(coroutineContext[Job])
    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Exception caught in conversation" }
    }

    private val coroutineScope = CoroutineScope(coroutineContext + Dispatchers.Default + job + handler)

    private fun newId(): String = idGenerator.newId()
    private fun newTimestamp(): Instant = timeProvider.now()

    private val _state = MutableStateFlow<ConversationState>(ConversationState.Loading)
    private val _events = MutableSharedFlow<Event>()
    private val _titleUpdates = MutableSharedFlow<String>(extraBufferCapacity = 64)
    private val _lastReadMessageIndexUpdates = MutableSharedFlow<Int>()

    private val userMember = Participant.User(id = newId(), name = "user")
    private val agentMember = Participant.Agent(id = newId(), name = "agent")
    val participants = listOf(userMember, agentMember)

    init {
        coroutineScope.launch {
            val digest = repository.getDigest(conversationId).stateIn(this)
            val transcript = repository.getEvents(conversationId).stateIn(this)

            val processing = transcript
                .mapNotNull { it.lastOrNull()?.payload as? AgentPresence.Processing }
                .map { it.isProcessing }
                .onStart { emit(false) }

            // Process repository changes
            launch {
                combine(digest, transcript, processing) { digest, transcript, processing ->
                    ConversationState.Loaded(
                        digest = digest,
                        transcript = transcript,
                        processing = processing,
                    )
                }.collect { _state.value = it }
            }

            // Observe new events
            launch {
                _events.collect { event ->
                    // Persist new events to repository
                    repository.appendEvent(conversationId, event)
                }
            }

            // Observe and persist title updates
            launch {
                _titleUpdates
                    .debounce(500.milliseconds)
                    .distinctUntilChanged()
                    .collect { newTitle ->
                        val current = digest.value
                        if (current.title != newTitle) {
                            repository.updateDigest(current.copy(title = newTitle))
                        }
                    }
            }

            // Observe and persist last read message index updates
            launch {
                _lastReadMessageIndexUpdates.collect { lastReadMessageIndex ->
                    val lastMessageIndex = transcript.value.count { it.isViewItem() } - 1
                    val unreadMessageCount = (lastMessageIndex - lastReadMessageIndex).coerceAtLeast(0)

                    val currentDigest = digest.value
                    if (currentDigest.lastReadMessageIndex != lastReadMessageIndex
                        || currentDigest.unreadMessageCount != unreadMessageCount
                    ) {
                        repository.updateDigest(
                            currentDigest.copy(
                                lastReadMessageIndex = lastReadMessageIndex,
                                unreadMessageCount = unreadMessageCount,
                            )
                        )
                    }
                }
            }

            // Restore agent
            val agentConfiguration = digest.value.agentConfiguration
            val agent = agentFactory.createAgent(agentConfiguration)
            agent.restorePrompt(transcript.value)

            launch {
                agent.joinConversation(newAgentView())
            }
        }
    }

    override fun close() {
        job.cancel()
    }

    private fun Event.isViewItem(): Boolean =
        payload !is AgentPresence.Processing && payload !is ToolUsage.Approval

    fun newUserView(): ConversationUserView = UserViewImpl(userMember)
    private fun newAgentView(): ConversationAgentView = AgentViewImpl(agentMember)

    private inner class AgentViewImpl(
        val participant: Participant,
    ) : ConversationAgentView {

        override val events: SharedFlow<Event> get() = _events

        override suspend fun sendProcessing(isProcessing: Boolean) =
            send(AgentPresence.Processing(isProcessing = isProcessing))

        override suspend fun sendMessage(content: List<Part>) =
            send(Messaging.Message(content = content))

        override suspend fun sendToolUseVetting(calls: List<Call>) =
            send(ToolUsage.Vetting(calls = calls))

        override suspend fun sendToolUseResult(call: Call, result: CallResult) =
            send(ToolUsage.Notification(call = call, result = result))

        override suspend fun send(payload: Event.Agent) {
            _events.emit(Event(id = newId(), timestamp = newTimestamp(), sender = participant, payload = payload))
        }
    }

    private inner class UserViewImpl(
        val participant: Participant,
    ) : ConversationUserView {

        override val state: StateFlow<ConversationState> get() = _state

        override val events: SharedFlow<Event> get() = _events

        override suspend fun updateTitle(title: String) {
            _titleUpdates.emit(title)
        }

        override suspend fun updateLastReadMessageIndex(index: Int) {
            _lastReadMessageIndexUpdates.emit(index)
        }

        override suspend fun sendMessage(content: List<Part>) =
            send(Messaging.Message(content = content))

        override suspend fun sendToolUseApproval(approvals: Map<Call, Boolean>) =
            send(ToolUsage.Approval(approvals = approvals))

        override suspend fun send(payload: Event.User) {
            _events.emit(Event(id = newId(), timestamp = newTimestamp(), sender = participant, payload = payload))
        }
    }
}
