package io.github.ptitjes.konvo.core.ai.spi

interface ModelProvider {
    val name: String

    suspend fun queryModelCards(): List<ModelCard>
}
