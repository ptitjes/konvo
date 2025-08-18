package io.github.ptitjes.konvo.core.characters

import kotlinx.coroutines.flow.*

/**
 * Dedicated manager for character cards.
 */
interface CharacterManager {
    /** A flow emitting the list of available character cards. */
    val characters: Flow<List<CharacterCard>>
}

/**
 * Retrieve a character by its name from the current characters emission or throw if not found.
 */
suspend fun CharacterManager.named(name: String): CharacterCard =
    characters.first().firstOrNull { it.name == name } ?: error("Model not found: $name")
