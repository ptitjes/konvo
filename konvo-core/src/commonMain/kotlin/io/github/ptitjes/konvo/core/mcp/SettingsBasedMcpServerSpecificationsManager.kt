package io.github.ptitjes.konvo.core.mcp

import io.github.ptitjes.konvo.core.settings.McpSettingsKey
import io.github.ptitjes.konvo.core.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlin.coroutines.CoroutineContext

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
