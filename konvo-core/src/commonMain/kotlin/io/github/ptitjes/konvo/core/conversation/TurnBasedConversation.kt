package io.github.ptitjes.konvo.core.conversation

import io.github.ptitjes.konvo.core.ai.koog.*
import kotlinx.coroutines.*

abstract class TurnBasedConversation(
    coroutineScope: CoroutineScope,
) : Conversation(coroutineScope) {
    protected abstract suspend fun buildChatAgent(): ChatAgent
    open fun getInitialAssistantMessage(): String? = null

    init {
        startConversation()
    }

    private fun startConversation() = launch {
        val agent = buildChatAgent()

        getInitialAssistantMessage()?.let {
            sendAssistantEvent(AssistantEvent.Message(it))
        }

        while (isActive) {
            val userMessage = awaitUserEvent()
            sendAssistantEvent(AssistantEvent.Processing)
            val result = agent.runAndGetResult(userMessage)
            result?.let { sendAssistantEvent(AssistantEvent.Message(it)) }
        }
    }
}
