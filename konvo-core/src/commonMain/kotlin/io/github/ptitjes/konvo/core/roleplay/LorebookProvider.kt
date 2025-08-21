package io.github.ptitjes.konvo.core.roleplay

/**
 * Dedicated provider interface for Lorebook entries.
 */
interface LorebookProvider {
    /** Optional display name for the provider (e.g., "Filesystem"). */
    val name: String?

    /** Query and return the list of available lorebooks. */
    suspend fun query(): List<Lorebook>
}
