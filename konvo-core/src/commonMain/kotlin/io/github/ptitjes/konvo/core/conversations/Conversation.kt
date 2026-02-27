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
        val transcript: List<Action<*>>,
    ) : ConversationState
}

@OptIn(FlowPreview::class)
class Conversation internal constructor(
    coroutineContext: CoroutineContext,
    val id: String,
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
    private val _events = MutableSharedFlow<Action<*>>()

    init {
        coroutineScope.launch {
            val digest = repository.getDigest(id).stateIn(this)
            val transcript = repository.getActions(id).stateIn(this)

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
                    repository.appendAction(id, event)
                }
            }

            val state = awaitConversationLoaded()
            restoreAgents(state.transcript)
        }
    }

    override fun close() {
        job.cancel()
    }

    private fun restoreAgents(transcript: List<Action<*>>) {
        val joins = transcript.filter {
            it.payload is Presence.Joining && it.sender is Participant.Agent
        }
    }

    suspend fun awaitConversationLoaded(): ConversationState.Loaded {
        return _state.filterIsInstance<ConversationState.Loaded>().first()
    }

    private fun checkConversationLoaded(): ConversationState.Loaded {
        check(_state.value is ConversationState.Loaded) { "Conversation not loaded yet" }
        return _state.value as ConversationState.Loaded
    }

    suspend fun join() {
        awaitConversationLoaded()
        newUserView().send(Presence.Joining)
    }

    fun newUserView(): ConversationUserView {
        checkConversationLoaded()
        // TODO get the current profile's user id
        val userParticipant = Participant.User(id = "user")
        return UserViewImpl(userParticipant)
    }

    suspend fun inviteAgent(agentConfiguration: AgentConfiguration) {
        val state = awaitConversationLoaded()
        val agent = agentFactory.createAgent(agentConfiguration)

        // TODO allow multiple agents
        val agentParticipant = Participant.Agent(id = "agent")
        val agentView = AgentViewImpl(agentParticipant)
        agent.restoreSession(state.transcript, agentView)
    }

    private inner class AgentViewImpl(
        override val participant: Participant.Agent,
    ) : InteractionDevice.Agent {

        override val actions: SharedFlow<Action<*>> get() = _events

        override suspend fun act(payload: Action.Agent) {
            _events.emit(
                Action(
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

        override val actions: SharedFlow<Action<*>> get() = _events

        override suspend fun act(payload: Action.User) {
            _events.emit(
                Action(
                    id = newId(),
                    timestamp = newTimestamp(),
                    sender = participant,
                    payload = payload,
                )
            )
        }
    }
}
