package io.github.ptitjes.konvo.plugin.roleplay

import kotlinx.coroutines.flow.*
import kotlinx.io.files.*

/**
 * Dedicated manager for characters.
 */
interface CharacterManager {
    /** A flow emitting any errors encountered by the manager. */
    val error: SharedFlow<String>

    /** A flow emitting the set of available character cards. */
    val characters: SharedFlow<Set<CharacterCard>>

    /**
     * Adds a character from a given [sourcePath].
     */
    fun add(sourcePath: Path)

    /**
     * Deletes a [CharacterCard].
     */
    fun delete(character: CharacterCard)
}

/**
 * Retrieves a character by its id or throws if not found.
 */
suspend fun CharacterManager.withId(id: String): CharacterCard =
    characters.first().firstOrNull { it.id == id } ?: error("Character not found: $id")
