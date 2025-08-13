@file:OptIn(ExperimentalTime::class)

package io.github.ptitjes.konvo.core.conversation

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.conversation.agents.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import io.github.ptitjes.konvo.core.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*
import kotlin.time.*

class LiveConversation(
    coroutineContext: CoroutineContext,
    private val conversationId: String,
    private val repository: ConversationRepository,
    private val timeProvider: TimeProvider = SystemTimeProvider,
    private val idGenerator: IdGenerator = UuidIdGenerator,
) : AutoCloseable {

    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val job = SupervisorJob()
    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Exception caught in conversation" }
    }

    private val coroutineScope = CoroutineScope(coroutineContext + job + handler)

    private val loaded = CompletableDeferred<Unit>()
    suspend fun awaitLoaded() = loaded.await()

    init {
        coroutineScope.launch {
            val conversation = repository.getConversation(conversationId).firstOrNull() ?: error("Invalid state")
            val events = repository.getEvents(conversationId).first()

            // Restore transcript
            this@LiveConversation.transcript.clear()
            events.forEach { event ->
                this@LiveConversation.transcript.append(event)
            }

            // Persist new events
            launch {
                this@LiveConversation.events.collect { event ->
                    this@LiveConversation.transcript.append(event)
                    repository.appendEvent(conversationId, event)
                }
            }

            // Restore agent
            val agentConfiguration = conversation.agentConfiguration
            val agent = agentConfiguration.buildAgent()
            agent.restorePrompt(events)

            launch {
                agent.joinConversation(newAgentView())
            }

            loaded.complete(Unit)
        }
    }

    override fun close() {
        job.cancel()
    }

    private fun newId(): String = idGenerator.newId()
    private fun newTimestamp(): Instant = timeProvider.now()

    private val userMember = Participant.User(id = newId(), name = "user")
    private val agentMember = Participant.Agent(id = newId(), name = "agent")
    val participants = listOf(userMember, agentMember)

    val transcript = Transcript()

    // Last read message index, -1 means nothing has been read yet
    private val _lastReadMessageIndex = MutableStateFlow(-1)
    val lastReadMessageIndex: StateFlow<Int> = _lastReadMessageIndex

    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events

    private suspend fun emitEvent(event: Event) {
        _events.emit(event)
    }

    fun newUserView(): ConversationUserView = UserViewImpl(userMember)
    private fun newAgentView(): ConversationAgentView = AgentViewImpl(agentMember)

    private inner class AgentViewImpl(
        val participant: Participant,
    ) : ConversationAgentView {
        override val conversation: LiveConversation = this@LiveConversation

        override val transcript: Transcript
            get() = conversation.transcript

        override val events: SharedFlow<Event>
            get() = conversation.events

        override suspend fun sendProcessing(isProcessing: Boolean) {
            emitEvent(
                Event.AssistantProcessing(
                    id = newId(),
                    timestamp = newTimestamp(),
                    source = participant,
                    isProcessing = isProcessing,
                )
            )
        }

        override suspend fun sendMessage(content: String) {
            emitEvent(
                Event.AssistantMessage(
                    id = newId(),
                    timestamp = newTimestamp(),
                    source = participant,
                    content = content
                )
            )
        }

        override suspend fun sendToolUseVetting(calls: List<ToolCall>): Event.ToolUseVetting {
            val event = Event.ToolUseVetting(
                id = newId(),
                timestamp = newTimestamp(),
                source = participant,
                calls = calls
            )
            emitEvent(event)
            return event
        }

        override suspend fun sendToolUseResult(
            call: ToolCall,
            result: ToolCallResult,
        ) {
            emitEvent(
                Event.ToolUseNotification(
                    id = newId(),
                    timestamp = newTimestamp(),
                    source = participant,
                    call = call,
                    result = result
                )
            )
        }
    }

    private inner class UserViewImpl(
        val participant: Participant,
    ) : ConversationUserView {
        override val conversation: LiveConversation = this@LiveConversation

        override val transcript: Transcript
            get() = conversation.transcript

        override val events: SharedFlow<Event>
            get() = conversation.events

        override val lastReadMessageIndex: StateFlow<Int>
            get() = conversation.lastReadMessageIndex

        override suspend fun updateLastReadMessageIndex(index: Int) {
            val clamped = index.coerceIn(-1, transcript.events.size - 1)
            if (clamped > _lastReadMessageIndex.value) {
                _lastReadMessageIndex.emit(clamped)
            }
        }

        override suspend fun sendMessage(
            content: String,
            attachments: List<Attachment>,
        ) {
            emitEvent(
                Event.UserMessage(
                    id = newId(),
                    timestamp = newTimestamp(),
                    source = participant,
                    content = content,
                    attachments = attachments
                )
            )
        }

        override suspend fun sendToolUseApproval(
            vetting: Event.ToolUseVetting,
            approvals: Map<ToolCall, Boolean>,
        ) {
            emitEvent(
                Event.ToolUseApproval(
                    id = newId(),
                    timestamp = newTimestamp(),
                    source = participant,
                    vetting = vetting,
                    approvals = approvals,
                )
            )
        }
    }
}
