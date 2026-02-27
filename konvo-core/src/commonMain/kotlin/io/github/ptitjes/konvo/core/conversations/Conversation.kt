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
    private val _events = MutableSharedFlow<ConversationEntry>()

    init {
        coroutineScope.launch {
            val transcript = repository.getTranscript(id).stateIn(this)

            // Process repository changes
            launch {
                transcript.collect { transcript ->
                    _state.value = ConversationState.Loaded(
                        digest = transcript.digest,
                        transcript = transcript.entries.filterIsInstance<Action<*>>(),
                    )
                }
            }

            // Observe new events
            launch {
                _events.collect { entry ->
                    // Persist new entries to repository
                    repository.appendEntry(id, entry)
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

        override val actions: SharedFlow<Action<*>>
            get() = _events.filterIsInstance<Action<*>>().shareIn(
                coroutineScope,
                SharingStarted.Eagerly,
                replay = 0
            )

        override suspend fun act(payload: Action.Agent, interaction: Interaction?) {
            _events.emit(
                Action(
                    id = newId(),
                    timestamp = newTimestamp(),
                    sender = participant,
                    payload = payload,
                    interaction = interaction,
                )
            )
        }

        override suspend fun startInteraction(
            protocol: InteractionProtocol,
            parent: Interaction?,
            trigger: Action<*>?
        ): Interaction {
            val interaction = Interaction(
                id = newId(),
                protocol = protocol,
                parent = parent,
                trigger = trigger,
            )
            _events.emit(
                InteractionBoundary.Start(
                    timestamp = newTimestamp(),
                    sender = participant,
                    interaction = interaction,
                )
            )
            return interaction
        }

        override suspend fun endInteraction(interaction: Interaction) {
            _events.emit(
                InteractionBoundary.End(
                    timestamp = newTimestamp(),
                    sender = participant,
                    interaction = interaction,
                )
            )
        }
    }

    private inner class UserViewImpl(
        override val participant: Participant.User,
    ) : ConversationUserView {

        override val state: StateFlow<ConversationState> get() = _state

        override val actions: SharedFlow<Action<*>>
            get() = _events.filterIsInstance<Action<*>>().shareIn(
                coroutineScope,
                SharingStarted.Eagerly,
                replay = 0
            )

        override suspend fun act(payload: Action.User, interaction: Interaction?) {
            _events.emit(
                Action(
                    id = newId(),
                    timestamp = newTimestamp(),
                    sender = participant,
                    payload = payload,
                    interaction = interaction,
                )
            )
        }
    }
}
