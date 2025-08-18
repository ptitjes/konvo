package io.github.ptitjes.konvo.core.ai.prompts

/**
 * Dedicated provider interface for PromptCard entries.
 *
 * This replaces usages of the generic Provider<PromptCard> to make
 * prompt provider semantics explicit in the codebase.
 */
interface PromptProvider {
    /** Optional display name for the provider (e.g., "MCP"). */
    val name: String?

    /** Query and return the list of available prompt cards. */
    suspend fun query(): List<PromptCard>
}
