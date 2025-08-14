package io.github.ptitjes.konvo.core.ai

interface ProviderManager<C> {
    val elements: List<C>

    suspend fun init()
}
