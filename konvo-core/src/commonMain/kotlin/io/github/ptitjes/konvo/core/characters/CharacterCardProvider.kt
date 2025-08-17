package io.github.ptitjes.konvo.core.characters

/**
 * Dedicated provider interface for CharacterCard entries.
 *
 * This replaces usages of the generic Provider<CharacterCard> to make
 * character provider semantics explicit in the codebase.
 */
interface CharacterCardProvider {
    /** Optional display name for the provider (e.g., "Filesystem"). */
    val name: String?

    /** Query and return the list of available character cards. */
    suspend fun query(): List<CharacterCard>
}
