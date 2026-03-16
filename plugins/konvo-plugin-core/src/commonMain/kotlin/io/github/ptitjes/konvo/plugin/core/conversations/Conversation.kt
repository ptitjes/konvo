@file:OptIn(ExperimentalTime::class)

package io.github.ptitjes.konvo.plugin.core.conversations

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.plugin.core.agents.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.events.*
import io.github.ptitjes.konvo.plugin.core.conversations.storage.*
import io.github.ptitjes.konvo.plugin.core.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*
import kotlin.time.*
import kotlin.uuid.*

sealed interface ConversationState {
    data object Loading : ConversationState
    data class Loaded(
        val digest: ConversationDigest,
        val transcript: ConversationTranscript,
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
                        transcript = transcript,
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

            watchAgents()
        }
    }

    override fun close() {
        job.cancel()
    }

    private val agentSessions = mutableMapOf<Participant.Agent, AgentSession>()

    private suspend fun restoreAgent(
        invite: Action<ConversationControl.InviteAgent>,
    ) {
        val state = checkConversationLoaded()

        val agentParticipant = Participant.Agent(id = invite.payload.participantId)
        val agentConfiguration = invite.payload.agentConfiguration
        val device = AgentDevice(agentParticipant)
        val agent = agentFactory.createAgent(agentConfiguration)
        val agentSession = agent.restoreSession(state.transcript, invite, device)
        agentSessions += agentParticipant to agentSession
    }

    private fun restoreAgents(transcript: ConversationTranscript) {
        val actions = transcript.actions

        val invites = actions
            .filter { it.payload is ConversationControl.InviteAgent }
            .map { it.asTypedAction<ConversationControl.InviteAgent>() }
            .filter { invite ->
                val participantId = invite.payload.participantId
                actions.none { it.payload is Presence.Leaving && it.sender.id == participantId }
            }

        invites.forEach { invite ->
            coroutineScope.launch { restoreAgent(invite) }
        }
    }

    private fun CoroutineScope.watchAgents() = launch {
        _events
            .filterIsInstance<Action<*>>()
            .filter { it.payload is ConversationControl.InviteAgent }
            .map { it.asTypedAction<ConversationControl.InviteAgent>() }
            .collect { invite ->
                coroutineScope.launch { restoreAgent(invite) }
            }
    }

    suspend fun awaitConversationLoaded(): ConversationState.Loaded {
        return _state.filterIsInstance<ConversationState.Loaded>().first()
    }

    private fun checkConversationLoaded(): ConversationState.Loaded {
        check(_state.value is ConversationState.Loaded) { "Conversation not loaded yet" }
        return _state.value as ConversationState.Loaded
    }

    fun newUserDevice(): InteractionDevice.User {
        checkConversationLoaded()
        // TODO get the current profile's user id
        val userParticipant = Participant.User(id = "user")
        return UserDevice(userParticipant)
    }

    suspend fun join() {
        awaitConversationLoaded()
        newUserDevice().act(Presence.Joining)
    }

    // TODO replace this with an AgentId, when configuration protocols are up
    suspend fun inviteAgent(agentConfiguration: AgentConfiguration) {
        awaitConversationLoaded()

        val participantId = Uuid.random().toString()

        newUserDevice().act(
            ConversationControl.InviteAgent(
                participantId = participantId,
                agentConfiguration = agentConfiguration
            )
        )
    }

    private inner class AgentDevice(
        override val participant: Participant.Agent,
    ) : InteractionDevice.Agent {

        override val actions: SharedFlow<Action<*>>
            get() = _events.filterIsInstance<Action<*>>().shareIn(
                coroutineScope,
                SharingStarted.Eagerly,
                replay = 0
            )

        override suspend fun <P : Action.Agent> act(
            payload: P,
            interaction: Interaction?,
        ): Action<P> {
            val action = Action(
                id = newId(),
                timestamp = newTimestamp(),
                sender = participant,
                interaction = interaction,
                payload = payload,
            )
            return action.also { _events.emit(it) }
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

    private inner class UserDevice(
        override val participant: Participant.User,
    ) : InteractionDevice.User {

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
                    interaction = interaction,
                    payload = payload,
                )
            )
        }
    }
}
