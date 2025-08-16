package io.github.ptitjes.konvo.core.ai

import io.github.ptitjes.konvo.core.ai.koog.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.settings.*
import kotlinx.coroutines.flow.*

class SettingsBasedModelProviderManager(
    private val settingsRepository: SettingsRepository,
) : ProviderManager<ModelCard> {

    private lateinit var _elements: List<ModelCard>
    override val elements: List<ModelCard>
        get() = _elements

    override suspend fun init() {
        val settings = settingsRepository.getSettings(ModelProviderSettingsKey).first()

        val providers = settings.providers.map { providerSettings ->
            when (providerSettings.configuration) {
                is ModelProviderConfiguration.Anthropic -> AnthropicModelProvider(
                    name = providerSettings.name,
                    apiKey = providerSettings.configuration.apiKey,
                )

                is ModelProviderConfiguration.Ollama -> OllamaModelProvider(
                    name = providerSettings.name,
                    baseUrl = providerSettings.configuration.url,
                )

                is ModelProviderConfiguration.OpenAI -> OpenAIModelProvider(
                    name = providerSettings.name,
                    apiKey = providerSettings.configuration.apiKey,
                )

                is ModelProviderConfiguration.Google -> GoogleModelProvider(
                    name = providerSettings.name,
                    apiKey = providerSettings.configuration.apiKey,
                )
            }
        }

        _elements = providers.flatMap { it.query() }
    }
}
