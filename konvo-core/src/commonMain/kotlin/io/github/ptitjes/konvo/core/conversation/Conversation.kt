package io.github.ptitjes.konvo.core.conversation

import io.github.oshai.kotlinlogging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.serialization.json.*
import kotlin.coroutines.*

abstract class Conversation(
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

    abstract val configuration: ConversationModeConfiguration

    protected val userEventsChannel = Channel<String>()
    protected val assistantEventsChannel = Channel<AssistantEvent>()

    val userEvents: SendChannel<String> = userEventsChannel
    val assistantEvents: ReceiveChannel<AssistantEvent> = assistantEventsChannel

    protected suspend fun awaitUserEvent(): String = userEventsChannel.receive()
    protected suspend fun sendAssistantEvent(event: AssistantEvent) = assistantEventsChannel.send(event)

    fun terminate() {
        job.cancel()
    }
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
