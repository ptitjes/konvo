package io.github.ptitjes.konvo.core.models

/**
 * Dedicated provider interface for AI ModelCard entries.
 *
 * This replaces usages of the generic Provider<ModelCard> to make
 * model provider semantics explicit in the codebase.
 */
interface ModelProvider {
    /** Optional display name for the provider (e.g., "Ollama", "OpenAI"). */
    val name: String?

    /** Query and return the list of available model cards. */
    suspend fun query(): List<Model>
}
