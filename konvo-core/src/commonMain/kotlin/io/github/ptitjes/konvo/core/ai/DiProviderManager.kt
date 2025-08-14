package io.github.ptitjes.konvo.core.ai

import io.github.ptitjes.konvo.core.ai.spi.*

class DiProviderManager<C>(
    private val providers: Set<Provider<C>>,
) : ProviderManager<C> {

    private lateinit var _elements: List<C>
    override val elements: List<C>
        get() = _elements

    override suspend fun init() {
        _elements = providers.flatMap { it.query() }
    }
}
