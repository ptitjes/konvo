package io.github.ptitjes.konvo.core.ai

import io.github.ptitjes.konvo.core.ai.spi.*

interface ProviderManager<C> {
    val elements: List<C>

    suspend fun init()
}

fun <C : NamedCard> ProviderManager<C>.named(name: String): C =
    elements.firstOrNull { it.name == name } ?: error("Model not found: $name")
