package io.github.ptitjes.konvo.core.conversation

import ai.koog.prompt.message.*
import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.ai.koog.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.serialization.json.*
import kotlin.coroutines.*

class Conversation(
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

    private val userEventsChannel = Channel<UserEvent>()
    private val assistantEventsChannel = Channel<AssistantEvent>()

    val userEvents: SendChannel<UserEvent> = userEventsChannel
    val assistantEvents: ReceiveChannel<AssistantEvent> = assistantEventsChannel

    suspend fun awaitUserEvent(): UserEvent = userEventsChannel.receive()
    suspend fun sendAssistantEvent(event: AssistantEvent) = assistantEventsChannel.send(event)

    fun addAgent(agent: ChatAgent) = launch {
        agent.joinConversation(this@Conversation)
    }

    fun terminate() {
        job.cancel()
    }
}

sealed interface UserEvent {
    data class Message(
        val content: String,
        val attachments: List<Attachment>,
    ) : UserEvent
}

sealed interface AssistantEvent {
    data object Processing : AssistantEvent
    data class Message(val content: String) : AssistantEvent
    data class ToolUseVetting(val calls: List<VetoableToolCall>) : AssistantEvent
    data class ToolUseResult(
        val tool: String,
        val arguments: Map<String, JsonElement>,
        val result: ToolCallResult,
    ) : AssistantEvent
}

interface VetoableToolCall {
    val tool: String
    val arguments: Map<String, JsonElement>

    fun allow()
    fun reject()
}

sealed interface ToolCallResult {
    data class Success(val text: String) : ToolCallResult
    data class ExecutionFailure(val reason: String) : ToolCallResult
}
