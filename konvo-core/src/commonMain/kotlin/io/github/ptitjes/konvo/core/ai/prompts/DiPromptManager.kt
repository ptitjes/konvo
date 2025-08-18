package io.github.ptitjes.konvo.core.ai.prompts

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

/**
 * Prompt manager backed by a set of PromptProviders.
 */
class DiPromptManager(
    coroutineContext: CoroutineContext,
    private val providers: Set<PromptProvider>,
) : PromptManager {

    private val job = SupervisorJob(coroutineContext[Job])
    private val coroutineScope = CoroutineScope(coroutineContext + job)

    override val prompts: Flow<List<PromptCard>> =
        flow { emit(providers.flatMap { it.query() }) }
            .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)
}
