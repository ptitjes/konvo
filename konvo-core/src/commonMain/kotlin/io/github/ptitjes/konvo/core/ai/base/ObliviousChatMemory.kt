package io.github.ptitjes.konvo.core.ai.base

import io.github.ptitjes.konvo.core.ai.spi.*

class ObliviousChatMemory(
    override val id: Any,
) : ChatMemory {

    private var systemMessage: ChatMessage.System? = null
    private var userMessage: ChatMessage.User? = null
    private val assistantAndToolMessages = mutableListOf<ChatMessage>()

    override val messages: List<ChatMessage>
        get() = buildList {
            if (systemMessage != null) add(systemMessage!!)
            if (userMessage != null) add(userMessage!!)
            addAll(assistantAndToolMessages)
        }

    override fun add(message: ChatMessage) {
        when (message) {
            is ChatMessage.System -> systemMessage = message
            is ChatMessage.User -> {
                userMessage = message
                assistantAndToolMessages.clear()
            }

            else -> assistantAndToolMessages.add(message)
        }
    }

    override fun clear() {
        systemMessage = null
        userMessage = null
        assistantAndToolMessages.clear()
    }
}
