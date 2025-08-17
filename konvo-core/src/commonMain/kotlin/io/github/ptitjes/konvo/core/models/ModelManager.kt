package io.github.ptitjes.konvo.core.models

/**
 * Dedicated manager for AI models.
 *
 * This replaces usages of ProviderManager<ModelCard> to make the model manager explicit.
 */
interface ModelManager {
    /** The list of available model cards. */
    val elements: List<Model>

    /** Initialize the manager (e.g., query providers, load settings). */
    suspend fun init()
}

/**
 * Retrieve a model by its name or throw if not found.
 */
fun ModelManager.named(name: String): Model =
    elements.firstOrNull { it.name == name } ?: error("Model not found: $name")
