package io.github.ptitjes.konvo.core.models

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

/**
 * Model manager backed by a set of ModelProviders.
 */
class DiModelManager(
    coroutineContext: CoroutineContext,
    private val providers: Set<ModelProvider>,
) : ModelManager {

    private val job = SupervisorJob(coroutineContext[Job])
    private val coroutineScope = CoroutineScope(coroutineContext + job)

    override val models: Flow<List<ModelCard>> =
        flow { emit(providers.flatMap { it.query() }) }
            .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)
}
