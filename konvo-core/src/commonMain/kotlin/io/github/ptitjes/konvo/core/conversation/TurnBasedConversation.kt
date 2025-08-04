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
            val userEvent = awaitUserEvent()
            sendAssistantEvent(AssistantEvent.Processing)
            when (userEvent) {
                is UserEvent.Message -> {
                    val result = agent.run(userEvent.toUserMessage())
                    result.forEach { sendAssistantEvent(it.toAssistantEventMessage()) }
                }
            }
        }
    }

    private fun UserEvent.Message.toUserMessage(): Message.User =
        Message.User(content, attachments = attachments, metaInfo = RequestMetaInfo.create(clock))

    private fun Message.Assistant.toAssistantEventMessage(): AssistantEvent.Message =
        AssistantEvent.Message(content)
}
