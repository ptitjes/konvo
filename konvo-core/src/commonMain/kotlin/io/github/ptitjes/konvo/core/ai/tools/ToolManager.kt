package io.github.ptitjes.konvo.core.ai.tools

import kotlinx.coroutines.flow.*

/**
 * Dedicated manager for tools.
 */
interface ToolManager {
    /** A flow emitting the list of available tool cards. */
    val tools: Flow<List<ToolCard>>
}

/**
 * Retrieve a tool by its name from the current tools emission or throw if not found.
 */
suspend fun ToolManager.named(name: String): ToolCard =
    tools.first().firstOrNull { it.name == name } ?: error("Model not found: $name")
