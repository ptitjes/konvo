package io.github.ptitjes.konvo.core.ai.spi

interface ChatMemory {
    val id: Any
    val messages: List<ChatMessage>
    fun add(message: ChatMessage)
    fun clear()
}
