package io.github.ptitjes.konvo.core.tools

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

/**
 * Tool manager backed by a set of ToolProviders.
 */
class DiToolManager(
    coroutineContext: CoroutineContext,
    private val providers: Set<ToolProvider>,
) : ToolManager {

    private val job = SupervisorJob(coroutineContext[Job])
    private val coroutineScope = CoroutineScope(coroutineContext + job)

    override val tools: Flow<List<ToolCard>> =
        flow { emit(providers.flatMap { it.query() }) }
            .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)
}
