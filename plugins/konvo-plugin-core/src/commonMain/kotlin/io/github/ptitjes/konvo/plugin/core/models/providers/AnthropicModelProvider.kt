package io.github.ptitjes.konvo.plugin.core.models.providers

import ai.koog.prompt.executor.clients.*
import ai.koog.prompt.executor.clients.anthropic.*
import ai.koog.prompt.llm.*
import io.github.ptitjes.konvo.plugin.core.models.*

class AnthropicModelProvider(
    override val name: String,
    private val apiKey: String,
) : ModelProvider {
    private val client by lazy { AnthropicLLMClient(apiKey) }

    override suspend fun queryModels(): List<ModelCard> {
        val knownModels = AnthropicModels.models

        val models = client.models()
        return models.mapNotNull { model ->
            val id = model.id
            val name = id.removeDateSuffix()

            val knownModel = knownModels.find { it.id == name } ?: return@mapNotNull null

            AnthropicModelCard(model = knownModel.copy(id = model.id), name = name.beautifyAnthropicModelName())
        }
    }

    private inner class AnthropicModelCard(
        private val model: LLModel,
        override val name: String,
    ) : ModelCard {
        override val provider: ModelProvider get() = this@AnthropicModelProvider
        override val size: Long? get() = null
        override val parameterCount: Long? get() = null
        override val contextLength: Long? get() = null
        override val quantizationLevel: String? get() = null
        override val supportsTools: Boolean
            get() {
                val capabilities = model.capabilities
                return capabilities != null && LLMCapability.Tools in capabilities
            }

        override fun toLLModel(): LLModel = model
        override fun getLLMClient(): LLMClient = AnthropicLLMClient(
            settings = AnthropicClientSettings(mapOf(model to model.id)),
            apiKey = this@AnthropicModelProvider.apiKey,
        )
    }
}

private fun String.beautifyAnthropicModelName(): String {
    val splitName = split('-')
    val (prefixParts, suffixParts) = splitName.partition { part -> !part.all { it.isDigit() } }

    return prefixParts.joinToString(" ") { it.capitalize() } + " " +
            suffixParts.joinToString(".")
}

private const val DATE_LENGTH = 8

private fun String.removeDateSuffix(dateLength: Int = DATE_LENGTH): String {
    val suffixLength = dateLength + 1
    val hasDateSuffix = length > suffixLength && get(length - suffixLength) == '-' &&
            ((length - dateLength)..<length).all { get(it).isDigit() }
    return if (hasDateSuffix) this.substring(0, length - suffixLength) else this
}

private fun String.capitalize(): String = replaceFirstChar { it.uppercase() }
