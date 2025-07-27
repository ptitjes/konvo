package io.github.ptitjes.konvo.core.conversation

import ai.koog.prompt.message.*
import io.github.ptitjes.konvo.core.ai.koog.*
import kotlinx.coroutines.*
import kotlinx.datetime.*

abstract class TurnBasedConversation(
    coroutineScope: CoroutineScope,
) : Conversation(coroutineScope) {
    protected abstract suspend fun buildChatAgent(): ChatAgent
    open fun getInitialAssistantMessage(): String? = null

    init {
        startConversation()
    }

    private val clock = Clock.System

    private fun startConversation() = launch {
        val agent = buildChatAgent()

        getInitialAssistantMessage()?.let {
            sendAssistantEvent(AssistantEvent.Message(it))
        }

        while (isActive) {
            val userMessage = awaitUserEvent()
            sendAssistantEvent(AssistantEvent.Processing)
            val result = agent.run(Message.User(userMessage, metaInfo = RequestMetaInfo.create(clock)))
            result.forEach { sendAssistantEvent(AssistantEvent.Message(it.content)) }
        }
    }
}
