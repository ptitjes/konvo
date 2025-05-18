package io.github.ptitjes.konvo.core.conversation

import io.github.ptitjes.konvo.core.ai.base.*
import io.github.ptitjes.konvo.core.ai.spi.*
import kotlinx.coroutines.*

abstract class TurnBasedConversation(
    coroutineScope: CoroutineScope,
) : Conversation(coroutineScope) {
    abstract fun buildModel(): ChatModel
    open fun getInitialAssistantMessage(): String? = null

    init {
        startConversation()
    }

    private fun startConversation() = launch {
        val model = buildModel()

        getInitialAssistantMessage()?.let {
            sendAssistantEvent(AssistantEvent.Message(it))
        }

        while (isActive) {
            val userMessage = ChatMessage.User(text = awaitUserEvent())

            sendAssistantEvent(AssistantEvent.Processing)
            val messages = model.chat(userMessage) { calls ->
                sendAssistantEvent(AssistantEvent.ToolUsePermission(calls))
            }
            messages.collect { message ->
                if (message is ChatMessage.Assistant) {
                    if (message.text.isNotBlank()) {
                        sendAssistantEvent(AssistantEvent.Message(message.text))
                    }
                } else if (message is ChatMessage.Tool) {
                    sendAssistantEvent(AssistantEvent.ToolUseResult(message.call, message.result))
                }
            }
        }
    }
}
