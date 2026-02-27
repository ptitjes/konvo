package io.github.ptitjes.konvo.core.models.providers

import ai.koog.prompt.executor.clients.*
import ai.koog.prompt.executor.clients.anthropic.*
import ai.koog.prompt.llm.*
import io.github.ptitjes.konvo.core.models.*

class AnthropicModelProvider(
    override val name: String,
    apiKey: String,
) : ModelProvider {
    private val client by lazy { AnthropicLLMClient(apiKey) }

    override suspend fun queryModels(): List<ModelCard> {
        return listOf(
            AnthropicModels.Sonnet_4 to "Sonnet 4",
            AnthropicModels.Sonnet_3_7 to "Sonnet 3.7",
            AnthropicModels.Sonnet_3_5 to "Sonnet 3.5",
            AnthropicModels.Opus_4 to "Opus 4",
            AnthropicModels.Opus_3 to "Opus 3",
            AnthropicModels.Haiku_3_5 to "Haiku 3.5",
            AnthropicModels.Haiku_3 to "Haiku 3",
        ).map { (model, name) -> AnthropicModelCard(model, name) }
    }

    private inner class AnthropicModelCard(
        private val delegate: LLModel,
        override val name: String,
    ) : ModelCard {
        override val provider: ModelProvider get() = this@AnthropicModelProvider
        override val size: Long? get() = null
        override val parameterCount: Long? get() = null
        override val contextLength: Long? get() = null
        override val quantizationLevel: String? get() = null
        override val supportsTools: Boolean get() {
            val capabilities = delegate.capabilities
            return capabilities != null && LLMCapability.Tools in capabilities
        }

        override fun toLLModel(): LLModel = delegate
        override fun getLLMClient(): LLMClient = this@AnthropicModelProvider.client
    }
}
