package io.github.ptitjes.konvo.core.models

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

/**
 * Model manager backed by a set of ModelProviders.
 */
class DiModelManager(
    coroutineContext: CoroutineContext,
    private val providers: Set<ModelProvider>,
) : ModelManager {

    private val job = SupervisorJob(coroutineContext[Job])
    private val coroutineScope = CoroutineScope(coroutineContext + job)

    override val models: Flow<List<Model>> =
        flow { emit(providers.flatMap { it.query() }) }
            .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)
}
