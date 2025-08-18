package io.github.ptitjes.konvo.core.mcp

import io.github.ptitjes.konvo.core.settings.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

class SettingsBasedMcpServerSpecificationsManager(
    coroutineContext: CoroutineContext,
    settingsRepository: SettingsRepository,
) : McpServerSpecificationsManager {

    private val job = SupervisorJob(coroutineContext[Job])
    private val coroutineScope = CoroutineScope(coroutineContext + Dispatchers.Default + job)

    override val specifications: Flow<Map<String, ServerSpecification>> =
        settingsRepository.getSettings(McpSettingsKey)
            .map { it.servers }
            .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)
}
