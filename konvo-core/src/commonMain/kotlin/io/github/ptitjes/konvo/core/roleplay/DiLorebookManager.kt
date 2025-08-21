package io.github.ptitjes.konvo.core.roleplay

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

/**
 * Lorebook manager backed by a set of LorebookProviders.
 */
class DiLorebookManager(
    coroutineContext: CoroutineContext,
    private val providers: Set<LorebookProvider>,
) : LorebookManager {

    private val job = SupervisorJob(coroutineContext[Job])
    private val coroutineScope = CoroutineScope(coroutineContext + job)

    override val lorebooks: Flow<List<Lorebook>> =
        flow { emit(providers.flatMap { it.query() }) }
            .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)
}
