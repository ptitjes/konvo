package io.github.ptitjes.konvo.core.ai.base

import io.github.ptitjes.konvo.core.ai.spi.*

class DefaultChatMemory(
    override val id: Any = Unit,
    private val memoryStore: ChatMemoryStore,
    private val evictionStrategy: EvictionStrategy,
) : ChatMemory {

    interface EvictionStrategy {
        fun ensureCapacity(messages: MutableList<ChatMessage>)
    }

    override val messages: List<ChatMessage> get() = memoryStore.get(id)

    override fun add(message: ChatMessage) {
        val messages = messages.toMutableList()
        if (message is ChatMessage.System) {
            messages.getOrNull(0).let { firstMessage ->
                if (firstMessage == null || firstMessage !is ChatMessage.System) {
                    messages.add(0, message)
                } else {
                    if (message == firstMessage) return
                    messages[0] = message
                }
            }
        } else {
            messages.add(message)
        }

        evictionStrategy.ensureCapacity(messages)
        memoryStore.update(id, messages)
    }

    override fun clear() {
        memoryStore.delete(id)
    }
}
