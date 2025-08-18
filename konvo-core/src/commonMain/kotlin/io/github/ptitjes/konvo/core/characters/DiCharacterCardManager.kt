package io.github.ptitjes.konvo.core.characters

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

/**
 * Character manager backed by a set of CharacterProviders.
 */
class DiCharacterManager(
    coroutineContext: CoroutineContext,
    private val providers: Set<CharacterProvider>,
) : CharacterManager {

    private val job = SupervisorJob(coroutineContext[Job])
    private val coroutineScope = CoroutineScope(coroutineContext + job)

    override val characters: Flow<List<CharacterCard>> =
        flow { emit(providers.flatMap { it.query() }) }
            .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)
}
