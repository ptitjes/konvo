package io.github.ptitjes.konvo.core.models

/**
 * Model manager backed by a set of ModelProviders.
 */
class DiModelManager(
    private val providers: Set<ModelProvider>,
) : ModelManager {

    private lateinit var _elements: List<Model>
    override val elements: List<Model>
        get() = _elements

    override suspend fun init() {
        _elements = providers.flatMap { it.query() }
    }
}
