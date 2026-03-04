package io.github.ptitjes.konvo.frontend.compose.models

import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.models.ModelProviderConfiguration.*
import io.github.ptitjes.konvo.core.models.providers.*
import io.github.ptitjes.konvo.frontend.compose.resources.Res
import io.github.ptitjes.konvo.frontend.compose.resources.ic_provider_anthropic
import io.github.ptitjes.konvo.frontend.compose.resources.ic_provider_google
import io.github.ptitjes.konvo.frontend.compose.resources.ic_provider_mistral
import io.github.ptitjes.konvo.frontend.compose.resources.ic_provider_ollama
import io.github.ptitjes.konvo.frontend.compose.resources.ic_provider_openai
import org.jetbrains.compose.resources.DrawableResource

/**
 * Resolves brand icons for model providers.
 */
object ModelProviderIcons {

    /**
     * Returns the [DrawableResource] icon for the given [provider], or `null` if no icon is available.
     */
    fun iconFor(provider: ModelProvider): DrawableResource? {
        return when (provider) {
            is OllamaModelProvider -> Res.drawable.ic_provider_ollama
            is AnthropicModelProvider -> Res.drawable.ic_provider_anthropic
            is OpenAIModelProvider -> Res.drawable.ic_provider_openai
            is GoogleModelProvider -> Res.drawable.ic_provider_google
            is MistralAIModelProvider -> Res.drawable.ic_provider_mistral
            else -> null
        }
    }

    /**
     * Returns the [DrawableResource] icon for the given [configuration], or `null` if no icon is available.
     */
    fun iconFor(configuration: ModelProviderConfiguration): DrawableResource? {
        return when (configuration) {
            is Ollama -> Res.drawable.ic_provider_ollama
            is Anthropic -> Res.drawable.ic_provider_anthropic
            is OpenAI -> Res.drawable.ic_provider_openai
            is Google -> Res.drawable.ic_provider_google
            is MistralAI -> Res.drawable.ic_provider_mistral
        }
    }
}
