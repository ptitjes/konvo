package io.github.ptitjes.konvo.core.roleplay

import kotlinx.coroutines.flow.*

/**
 * Dedicated manager for lorebooks.
 */
interface LorebookManager {
    /** A flow emitting the list of available lorebooks. */
    val lorebooks: Flow<List<Lorebook>>
}

/**
 * Retrieve a lorebook by its name from the current emission or throw if not found.
 */
suspend fun LorebookManager.withId(id: String): Lorebook =
    lorebooks.first().firstOrNull { it.id == id } ?: error("Lorebook not found: $id")
