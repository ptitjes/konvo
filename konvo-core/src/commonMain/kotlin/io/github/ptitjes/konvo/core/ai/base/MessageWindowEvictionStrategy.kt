package io.github.ptitjes.konvo.core.ai.base

import io.github.ptitjes.konvo.core.ai.spi.*

class MessageWindowEvictionStrategy(
    private val maxMessageCount: Int,
) : DefaultChatMemory.EvictionStrategy {

    init {
        require(maxMessageCount > 0) { "maxMessageCount must be strictly positive." }
    }

    override fun ensureCapacity(messages: MutableList<ChatMessage>) {
        while (messages.size > maxMessageCount) {
            val evictionIndex = if (messages[0] !is ChatMessage.System) 0 else 1

            val evicted = messages.removeAt(evictionIndex)
            if (evicted is ChatMessage.Assistant && evicted.hasToolCalls()) {
                while (messages.size > evictionIndex && messages[evictionIndex] is ChatMessage.Tool) {
                    messages.removeAt(evictionIndex)
                }
            }
        }
    }
}
