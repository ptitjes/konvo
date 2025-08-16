package io.github.ptitjes.konvo.core.settings

import kotlinx.serialization.*

@Serializable
data class ModelProviderSettings(
    val providers: List<NamedModelProvider> = emptyList(),
)

@Serializable
sealed interface ModelProviderConfiguration {
    @Serializable
    @SerialName("ollama")
    data class Ollama(val url: String) : ModelProviderConfiguration

    @Serializable
    @SerialName("anthropic")
    data class Anthropic(val apiKey: String) : ModelProviderConfiguration
}

@Serializable
data class NamedModelProvider(
    val name: String,
    val configuration: ModelProviderConfiguration,
)

/**
 * Key for model provider settings.
 */
val ModelProviderSettingsKey: SettingsSectionKey<ModelProviderSettings> = SettingsSectionKey(
    name = "model-providers",
    defaultValue = ModelProviderSettings(),
    serializer = ModelProviderSettings.serializer(),
)
