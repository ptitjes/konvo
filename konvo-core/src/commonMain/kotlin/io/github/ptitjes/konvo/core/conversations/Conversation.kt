@file:OptIn(ExperimentalTime::class)

package io.github.ptitjes.konvo.core.conversations

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.core.conversations.storage.*
import io.github.ptitjes.konvo.core.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*
import kotlin.time.*

sealed interface ConversationState {
    data object Loading : ConversationState
    data class Loaded(
        val digest: ConversationDigest,
        val transcript: List<Event<*>>,
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
    private val _events = MutableSharedFlow<Event<*>>()

    init {
        coroutineScope.launch {
            val digest = repository.getDigest(conversationId).stateIn(this)
            val transcript = repository.getEvents(conversationId).stateIn(this)

            // Process repository changes
            launch {
                combine(digest, transcript) { digest, transcript ->
                    ConversationState.Loaded(
                        digest = digest,
                        transcript = transcript,
                    )
                }.collect {
                    _state.value = it
                }
            }

            // Observe new events
            launch {
                _events.collect { event ->
                    // Persist new events to repository
                    repository.appendEvent(conversationId, event)
                }
            }

            awaitConversationLoaded()

            if (transcript.value.isEmpty()) {
                newUserView().send(Presence.Joining)
            }

            launch {
                // Restore agent
                val agentConfiguration = digest.value.agentConfiguration
                val agent = agentFactory.createAgent(agentConfiguration)

                agent.restoreSession(transcript.value, newAgentView())
            }
        }
    }

    override fun close() {
        job.cancel()
    }

    suspend fun awaitConversationLoaded() {
        _state.first { it is ConversationState.Loaded }
    }

    private fun checkConversationLoaded(): ConversationState.Loaded {
        check(_state.value is ConversationState.Loaded) { "Conversation not loaded yet" }
        return _state.value as ConversationState.Loaded
    }

    fun newUserView(): ConversationUserView {
        val state = checkConversationLoaded()
        val userParticipant = state.digest.participants.filterIsInstance<Participant.User>().first()
        return UserViewImpl(userParticipant)
    }

    private fun newAgentView(): ConversationAgentView {
        val state = checkConversationLoaded()
        val agentParticipant = state.digest.participants.filterIsInstance<Participant.Agent>().first()
        return AgentViewImpl(agentParticipant)
    }

    private inner class AgentViewImpl(
        override val participant: Participant.Agent,
    ) : ConversationAgentView {

        override val events: SharedFlow<Event<*>> get() = _events

        override suspend fun send(payload: Event.Agent) {
            _events.emit(
                Event(
                    id = newId(),
                    timestamp = newTimestamp(),
                    sender = participant,
                    payload = payload,
                )
            )
        }
    }

    private inner class UserViewImpl(
        override val participant: Participant.User,
    ) : ConversationUserView {

        override val state: StateFlow<ConversationState> get() = _state

        override val events: SharedFlow<Event<*>> get() = _events

        override suspend fun send(payload: Event.User) {
            _events.emit(
                Event(
                    id = newId(),
                    timestamp = newTimestamp(),
                    sender = participant,
                    payload = payload,
                )
            )
        }
    }
}
