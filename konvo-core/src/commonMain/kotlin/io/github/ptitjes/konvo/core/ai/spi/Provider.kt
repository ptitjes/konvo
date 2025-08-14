package io.github.ptitjes.konvo.core.ai.spi

interface Provider<C> {
    val name: String?

    suspend fun query(): List<C>
}
