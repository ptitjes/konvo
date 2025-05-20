package io.github.ptitjes.konvo.core.ai.spi

interface ChatModel {
    suspend fun withTokenCount(message: ChatMessage): ChatMessage

    suspend fun chat(
        context: List<ChatMessage>,
        tools: List<Tool>? = null,
        format: Format? = null,
    ): ChatMessage.Assistant
}
