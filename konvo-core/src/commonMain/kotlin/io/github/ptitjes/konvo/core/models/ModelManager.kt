package io.github.ptitjes.konvo.core.models

import kotlinx.coroutines.flow.*

/**
 * Dedicated manager for AI models.
 *
 * This replaces usages of ProviderManager<ModelCard> to make the model manager explicit.
 */
interface ModelManager {
    /** A flow of the available model cards. */
    val models: Flow<List<ModelCard>>
}

/**
 * Retrieve a model by its name or throw if not found.
 */
suspend fun ModelManager.named(name: String): ModelCard =
    models.first().firstOrNull { it.name == name } ?: error("Model not found: $name")
