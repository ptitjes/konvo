package io.github.ptitjes.konvo.core.ai.prompts

import kotlinx.coroutines.flow.*

/**
 * Dedicated manager for prompts.
 */
interface PromptManager {
    /** A flow emitting the list of available prompt cards. */
    val prompts: Flow<List<PromptCard>>
}

/**
 * Retrieve a prompt by its name from the current prompts emission or throw if not found.
 */
suspend fun PromptManager.named(name: String): PromptCard =
    prompts.first().firstOrNull { it.name == name } ?: error("Model not found: $name")
