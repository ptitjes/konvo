package io.github.ptitjes.konvo.core.ai.base

import io.github.ptitjes.konvo.core.ai.spi.*

class TokenWindowEvictionStrategy(
    private val maxTokenCount: Int,
) : DefaultChatMemory.EvictionStrategy {

    init {
        require(maxTokenCount > 0) { "maxTokenCount must be strictly positive." }
    }

    override fun ensureCapacity(messages: MutableList<ChatMessage>) {
        if (messages.isEmpty()) return

        var currentTokenCount = messages.sumOf { it.tokenCount }
        while (currentTokenCount > maxTokenCount) {
            val evictionIndex = if (messages[0] !is ChatMessage.System) 0 else 1

            val evicted = messages.removeAt(evictionIndex)
            currentTokenCount -= evicted.tokenCount
            if (evicted is ChatMessage.Assistant && evicted.hasToolCalls()) {
                while (messages.size > evictionIndex && messages[evictionIndex] is ChatMessage.Tool) {
                    messages.removeAt(evictionIndex).also {
                        currentTokenCount -= it.tokenCount
                    }
                }
            }
        }
    }
}

private val ChatMessage.tokenCount: Int
    get() {
        val metadata = metadata
        if (metadata == null) error("ChatMessage has no metadata")
        return metadata.tokenCount
    }
