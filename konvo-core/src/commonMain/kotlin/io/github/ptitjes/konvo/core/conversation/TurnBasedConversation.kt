package io.github.ptitjes.konvo.core.conversation

import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.spi.*
import kotlinx.coroutines.*

abstract class TurnBasedConversation(
    coroutineScope: CoroutineScope,
) : Conversation(coroutineScope) {
    abstract fun buildModel(): Model
    abstract fun buildSystemPrompt(): String
    open fun buildInitialAssistantMessage(): String? = null

    init {
        startConversation()
    }

    private val context = mutableListOf<ChatMessage>()

    private fun addMessageToContext(message: ChatMessage) {
        println(message)
        context.add(message)
    }

    private fun startConversation() = launch {
        val model = buildModel()

        model.preload()

        val message = ChatMessage.System(text = buildSystemPrompt())
        addMessageToContext(message)

        val assistantMessage = buildInitialAssistantMessage()
        if (assistantMessage != null) {
            addMessageToContext(ChatMessage.Assistant(text = assistantMessage))
            sendAssistantEvent(AssistantEvent.Message(assistantMessage))
        }

        while (isActive) {
            val content = awaitUserEvent()
            addMessageToContext(ChatMessage.User(text = content))

            sendAssistantEvent(AssistantEvent.Processing)
            val messages = model.chat(context) { calls ->
                sendAssistantEvent(AssistantEvent.ToolUsePermission(calls))
            }
            messages.forEach { message ->
                addMessageToContext(message)
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
