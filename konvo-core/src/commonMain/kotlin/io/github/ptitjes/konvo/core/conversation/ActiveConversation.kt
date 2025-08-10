@file:OptIn(ExperimentalTime::class)

package io.github.ptitjes.konvo.core.conversation

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.ai.koog.*
import io.github.ptitjes.konvo.core.conversation.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*
import kotlin.time.*
import kotlin.uuid.*

class ActiveConversation(
    coroutineScope: CoroutineScope,
) : CoroutineScope {

    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val job = SupervisorJob()
    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Exception caught in conversation" }
    }

    override val coroutineContext: CoroutineContext = coroutineScope.coroutineContext + job + handler

    private val clock = Clock.System

    private fun newTimestamp(): Instant = clock.now()

    private val userMember = Participant.User(
        id = newUniqueId(),
        name = "user"
    )

    private val agentMember = Participant.Agent(
        id = newUniqueId(),
        name = "agent"
    )

    val participants = listOf(userMember, agentMember)

    val transcript = Transcript()

    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events

    private suspend fun emitEvent(event: Event) {
        transcript.append(event)
        _events.emit(event)
    }

    private inner class AgentViewImpl(
        val participant: Participant,
    ) : ConversationAgentView {
        override val conversation: ActiveConversation = this@ActiveConversation

        override val transcript: Transcript
            get() = conversation.transcript

        override val events: SharedFlow<Event>
            get() = conversation.events

        override suspend fun sendProcessing(isProcessing: Boolean) {
            emitEvent(
                Event.AssistantProcessing(
                    id = newUniqueId(),
                    timestamp = newTimestamp(),
                    source = participant,
                    isProcessing = isProcessing,
                )
            )
        }

        override suspend fun sendMessage(content: String) {
            emitEvent(
                Event.AssistantMessage(
                    id = newUniqueId(),
                    timestamp = newTimestamp(),
                    source = participant,
                    content = content
                )
            )
        }

        override suspend fun sendToolUseVetting(calls: List<ToolCall>): Event.ToolUseVetting {
            val event = Event.ToolUseVetting(
                id = newUniqueId(),
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
                    id = newUniqueId(),
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
        override val conversation: ActiveConversation = this@ActiveConversation

        override val transcript: Transcript
            get() = conversation.transcript

        override val events: SharedFlow<Event>
            get() = conversation.events

        override suspend fun sendMessage(
            content: String,
            attachments: List<Attachment>,
        ) {
            emitEvent(
                Event.UserMessage(
                    id = newUniqueId(),
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
                    id = newUniqueId(),
                    timestamp = newTimestamp(),
                    source = participant,
                    vetting = vetting,
                    approvals = approvals,
                )
            )
        }
    }

    fun newUiView(): ConversationUserView = UserViewImpl(userMember)

    fun addAgent(agent: ChatAgent) = launch {
        agent.joinConversation(AgentViewImpl(agentMember))
    }

    fun terminate() {
        job.cancel()
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun newUniqueId(): String = Uuid.random().toString()
