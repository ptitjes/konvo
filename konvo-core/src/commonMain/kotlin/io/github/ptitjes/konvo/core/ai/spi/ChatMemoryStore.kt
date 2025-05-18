package io.github.ptitjes.konvo.core.ai.spi

interface ChatMemoryStore {
    fun get(memoryId: Any): List<ChatMessage>
    fun update(memoryId: Any, messages: List<ChatMessage>)
    fun delete(memoryId: Any)
}
