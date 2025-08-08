@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package io.github.ptitjes.konvo.core.conversation

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.ai.koog.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
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

    private val userMember = ConversationMember.User(
        id = newEventId(),
        name = "user"
    )

    private val agentMember = ConversationMember.Agent(
        id = newEventId(),
        name = "user"
    )

    private val members = listOf<ConversationMember>(

    )

    private val events = MutableSharedFlow<ConversationEvent>()


    private suspend fun emitEvent(event: ConversationEvent) {
        events.emit(event)
    }

    private val clock = Clock.System

    private fun newTimestamp(): Instant = clock.now()

    private fun newEventId(): Uuid = Uuid.random()

    private inner class AgentViewImpl(
        val conversationMember: ConversationMember,
    ) : ConversationAgentView {
        override val events: SharedFlow<ConversationEvent> = this@ActiveConversation.events

        override suspend fun sendProcessing() {
            emitEvent(
                ConversationEvent.AssistantProcessing(
                    id = newEventId(),
                    timestamp = newTimestamp(),
                    source = conversationMember
                )
            )
        }

        override suspend fun sendMessage(content: String) {
            emitEvent(
                ConversationEvent.AssistantMessage(
                    id = newEventId(),
                    timestamp = newTimestamp(),
                    source = conversationMember,
                    content = content
                )
            )
        }

        override suspend fun sendToolUseVetting(calls: List<VetoableToolCall>) {
            emitEvent(
                ConversationEvent.AssistantToolUseVetting(
                    id = newEventId(),
                    timestamp = newTimestamp(),
                    source = conversationMember,
                    calls = calls
                )
            )
        }

        override suspend fun sendToolUseResult(
            tool: String,
            arguments: Map<String, JsonElement>,
            result: ToolCallResult,
        ) {
            emitEvent(
                ConversationEvent.AssistantToolUseResult(
                    id = newEventId(),
                    timestamp = newTimestamp(),
                    source = conversationMember,
                    tool = tool,
                    arguments = arguments,
                    result = result
                )
            )
        }
    }

    private inner class UserViewImpl(
        val conversationMember: ConversationMember,
    ) : ConversationUserView {
        override val events: SharedFlow<ConversationEvent> = this@ActiveConversation.events

        override suspend fun sendMessage(
            content: String,
            attachments: List<Attachment>,
        ) {
            emitEvent(
                ConversationEvent.UserMessage(
                    id = newEventId(),
                    timestamp = newTimestamp(),
                    source = conversationMember,
                    content = content,
                    attachments = attachments
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
