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

    private suspend fun loadModels(settings: ModelProviderSettings): List<ModelCard> =
        withContext(Dispatchers.Default) {
            settings.providers.flatMap { providerSettings ->
                modelCache.getOrPut(providerSettings.configuration) {
                    providerSettings.loadModels()
                }
            }
        }

}

suspend fun NamedModelProvider.test() = runCatching { loadModels().let { } }

private suspend fun NamedModelProvider.loadModels(): List<ModelCard> = withContext(Dispatchers.IO) {
    buildModelProvider().query()
}

private fun NamedModelProvider.buildModelProvider(): ModelProvider = when (configuration) {
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
