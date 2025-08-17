package io.github.ptitjes.konvo.core.characters

/**
 * Dedicated manager for character cards.
 */
interface CharacterCardManager {
    /** The list of available character cards. */
    val elements: List<CharacterCard>

    /** Initialize the manager (e.g., query providers, load settings). */
    suspend fun init()
}

/**
 * Retrieve a character by its name or throw if not found.
 */
fun CharacterCardManager.named(name: String): CharacterCard =
    elements.firstOrNull { it.name == name } ?: error("Model not found: $name")
