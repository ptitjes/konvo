package io.github.ptitjes.konvo.core.conversation

import io.github.ptitjes.konvo.core.ai.spi.ToolCall
import io.github.ptitjes.konvo.core.ai.spi.ToolCallResult
import io.github.ptitjes.konvo.core.ai.spi.VetoableToolCall
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.coroutines.*

abstract class Conversation(
    coroutineScope: CoroutineScope,
) : CoroutineScope {
    private val job = SupervisorJob()
    private val handler = CoroutineExceptionHandler { _, exception ->
        // TODO use a logger
        println("Exception caught in conversation: $exception")
        exception.printStackTrace()
    }

    override val coroutineContext: CoroutineContext = coroutineScope.coroutineContext + job + handler

    abstract val configuration: ConversationModeConfiguration

    protected val userEventsChannel = Channel<String>()
    protected val assistantEventsChannel = Channel<AssistantEvent>()

    val userEvents: SendChannel<String> = userEventsChannel
    val assistantEvents: ReceiveChannel<AssistantEvent> = assistantEventsChannel

    open val hasInitialAssistantMessage: Boolean get() = false

    protected suspend fun awaitUserEvent(): String = userEventsChannel.receive()
    protected suspend fun sendAssistantEvent(event: AssistantEvent) = assistantEventsChannel.send(event)

    fun terminate() {
        job.cancel()
    }
}

sealed interface AssistantEvent {
    data object Processing : AssistantEvent
    data class Message(val content: String) : AssistantEvent
    data class ToolUsePermission(val calls: List<VetoableToolCall>) : AssistantEvent
    data class ToolUseResult(val call: ToolCall, val result: ToolCallResult) : AssistantEvent
}
