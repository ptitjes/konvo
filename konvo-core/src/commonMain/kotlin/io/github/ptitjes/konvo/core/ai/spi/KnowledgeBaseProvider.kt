package io.github.ptitjes.konvo.core.ai.spi

interface KnowledgeBaseProvider {
    val name: String

    suspend fun queryKnowledgeBases(): List<KnowledgeBaseCard>
}
