@file:OptIn(ExperimentalTime::class)

package io.github.ptitjes.konvo.core.conversation

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.ai.koog.*
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

    private val userMember = ConversationMember.User(
        id = newUniqueId(),
        name = "user"
    )

    private val agentMember = ConversationMember.Agent(
        id = newUniqueId(),
        name = "agent"
    )

    private val members = listOf<ConversationMember>(

    )

    val transcript = ConversationTranscript()

    private val _events = MutableSharedFlow<ConversationEvent>()
    val events: SharedFlow<ConversationEvent> = _events

    private suspend fun emitEvent(event: ConversationEvent) {
        transcript.append(event)
        _events.emit(event)
    }

    private inner class AgentViewImpl(
        val conversationMember: ConversationMember,
    ) : ConversationAgentView {
        override val conversation: ActiveConversation = this@ActiveConversation

        override suspend fun sendProcessing() {
            emitEvent(
                ConversationEvent.AssistantProcessing(
                    id = newUniqueId(),
                    timestamp = newTimestamp(),
                    source = conversationMember
                )
            )
        }

        override suspend fun sendMessage(content: String) {
            emitEvent(
                ConversationEvent.AssistantMessage(
                    id = newUniqueId(),
                    timestamp = newTimestamp(),
                    source = conversationMember,
                    content = content
                )
            )
        }

        override suspend fun sendToolUseVetting(calls: List<ToolCall>): ConversationEvent.AssistantToolUseVetting {
            val event = ConversationEvent.AssistantToolUseVetting(
                id = newUniqueId(),
                timestamp = newTimestamp(),
                source = conversationMember,
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
                ConversationEvent.AssistantToolUseResult(
                    id = newUniqueId(),
                    timestamp = newTimestamp(),
                    source = conversationMember,
                    call = call,
                    result = result
                )
            )
        }
    }

    private inner class UserViewImpl(
        val conversationMember: ConversationMember,
    ) : ConversationUserView {
        override val conversation: ActiveConversation = this@ActiveConversation

        override suspend fun sendMessage(
            content: String,
            attachments: List<Attachment>,
        ) {
            emitEvent(
                ConversationEvent.UserMessage(
                    id = newUniqueId(),
                    timestamp = newTimestamp(),
                    source = conversationMember,
                    content = content,
                    attachments = attachments
                )
            )
        }

        override suspend fun sendToolUseApproval(
            vetting: ConversationEvent.AssistantToolUseVetting,
            approvals: Map<ToolCall, Boolean>,
        ) {
            emitEvent(
                ConversationEvent.ToolUseApproval(
                    id = newUniqueId(),
                    timestamp = newTimestamp(),
                    source = conversationMember,
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
