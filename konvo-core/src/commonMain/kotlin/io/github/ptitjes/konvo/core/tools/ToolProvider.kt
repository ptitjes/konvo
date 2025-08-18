package io.github.ptitjes.konvo.core.tools

/**
 * Dedicated provider interface for ToolCard entries.
 *
 * This replaces usages of the generic Provider<ToolCard> to make
 * tool provider semantics explicit in the codebase.
 */
interface ToolProvider {
    /** Optional display name for the provider (e.g., "MCP"). */
    val name: String?

    /** Query and return the list of available tool cards. */
    suspend fun query(): List<ToolCard>
}
