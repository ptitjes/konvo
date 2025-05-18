package io.github.ptitjes.konvo.core.ai.spi

interface ModelProvider {
    val name: String

    suspend fun queryModels(): List<ModelCard>

    suspend fun chat(
        modelCard: ModelCard,
        context: List<ChatMessage>,
        tools: List<Tool>? = null,
    ): List<ChatMessage>
}
