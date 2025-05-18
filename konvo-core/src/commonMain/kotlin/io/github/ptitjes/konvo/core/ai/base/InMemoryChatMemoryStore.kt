package io.github.ptitjes.konvo.core.ai.base

import io.github.ptitjes.konvo.core.ai.spi.*

class InMemoryChatMemoryStore : ChatMemoryStore {
    private val store = mutableMapOf<Any, List<ChatMessage>>()

    override fun get(memoryId: Any): List<ChatMessage> = store[memoryId] ?: emptyList()

    override fun update(memoryId: Any, messages: List<ChatMessage>) {
        store[memoryId] = messages
    }

    override fun delete(memoryId: Any) {
        store.remove(memoryId)
    }
}
