package io.github.ptitjes.konvo.core.ai.base

import io.github.ptitjes.konvo.core.ai.spi.*

class MessageWindowChatMemory(
    override val id: Any = Unit,
    private val maxMessageCount: Int,
    private val memoryStore: ChatMemoryStore,
) : ChatMemory {

    init {
        require(maxMessageCount > 0) { "maxMessageCount must be strictly positive." }
    }

    override val messages: List<ChatMessage> get() = memoryStore.get(id)

    override fun add(message: ChatMessage) {
        val messages = messages.toMutableList()
        if (message is ChatMessage.System) {
            messages.getOrNull(0).let { firstMessage ->
                if (firstMessage == null || firstMessage !is ChatMessage.System) {
                    messages.add(0, message)
                } else if (message != firstMessage) {
                    messages[0] = message
                }
            }
        } else {
            messages.add(message)
        }

        ensureCapacity(messages)
        memoryStore.update(id, messages)
    }

    private fun ensureCapacity(messages: MutableList<ChatMessage>) {
        while (messages.size > maxMessageCount) {
            val evictionIndex = if (messages[0] !is ChatMessage.System) 0 else 1

            val evicted = messages.removeAt(evictionIndex)
            if (evicted is ChatMessage.Assistant && evicted.hasToolCalls()) {
                while (messages.size > evictionIndex && messages[evictionIndex] is ChatMessage.Tool) {
                    messages.removeAt(evictionIndex)
                }
            }
        }
        println(">>> messages after eviction: ${messages.joinToString("\n")}")
    }

    override fun clear() {
        memoryStore.delete(id)
    }
}
