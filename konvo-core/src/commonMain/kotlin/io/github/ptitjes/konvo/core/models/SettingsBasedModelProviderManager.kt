package io.github.ptitjes.konvo.core.models

import io.github.ptitjes.konvo.core.models.providers.*
import io.github.ptitjes.konvo.core.settings.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

class SettingsBasedModelProviderManager(
    coroutineContext: CoroutineContext,
    settingsRepository: SettingsRepository,
) : ModelManager {

    private val job = SupervisorJob(coroutineContext[Job])
    private val coroutineScope = CoroutineScope(coroutineContext + job)

    override val models: Flow<List<ModelCard>> = settingsRepository.getSettings(ModelProviderSettingsKey)
        .map { providerSettings -> loadModels(providerSettings) }
        .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)

    private val modelCache = mutableMapOf<ModelProviderConfiguration, List<ModelCard>>()

    private suspend fun loadModels(settings: ModelProviderSettings): List<ModelCard> {
        return settings.providers.flatMap { providerSettings ->
            modelCache.getOrPut(providerSettings.configuration) {
                loadModels(providerSettings.name, providerSettings.configuration)
            }
        }
    }

    private suspend fun loadModels(
        name: String,
        configuration: ModelProviderConfiguration,
    ): List<ModelCard> {
        val provider = when (configuration) {
            is ModelProviderConfiguration.Anthropic -> AnthropicModelProvider(
                name = name,
                apiKey = configuration.apiKey,
            )

            is ModelProviderConfiguration.Ollama -> OllamaModelProvider(
                name = name,
                baseUrl = configuration.url,
            )

            is ModelProviderConfiguration.OpenAI -> OpenAIModelProvider(
                name = name,
                apiKey = configuration.apiKey,
            )

            is ModelProviderConfiguration.Google -> GoogleModelProvider(
                name = name,
                apiKey = configuration.apiKey,
            )
        }

        return provider.query()
    }
}
