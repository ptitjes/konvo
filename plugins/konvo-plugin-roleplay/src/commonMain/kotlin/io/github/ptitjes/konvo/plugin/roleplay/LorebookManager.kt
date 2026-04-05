package io.github.ptitjes.konvo.plugin.roleplay

import kotlinx.coroutines.flow.*
import kotlinx.io.files.*

/**
 * Dedicated manager for lorebooks.
 */
interface LorebookManager {
    /** A flow emitting any errors encountered by the manager. */
    val error: SharedFlow<String>

    /** A flow emitting the set of available lorebooks. */
    val lorebooks: SharedFlow<Set<Lorebook>>

    /**
     * Adds a lorebook from a given [sourcePath].
     */
    fun add(sourcePath: Path)

    /**
     * Deletes a [lorebook].
     */
    fun delete(lorebook: Lorebook)
}

/**
 * Retrieves a lorebook by its id or throws if not found.
 */
suspend fun LorebookManager.withId(id: String): Lorebook =
    lorebooks.first().firstOrNull { it.id == id } ?: error("Lorebook not found: $id")
