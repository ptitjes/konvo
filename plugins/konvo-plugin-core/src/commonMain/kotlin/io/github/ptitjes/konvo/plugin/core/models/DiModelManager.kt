package io.github.ptitjes.konvo.plugin.core.models

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
        flow { emit(providers.flatMap { it.queryModels() }) }
            .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)

    override val providersInError: Flow<List<String>?> = flow { }
}
