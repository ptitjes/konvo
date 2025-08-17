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

    override suspend fun query(): List<Model> {
        return listOf(
            AnthropicModels.Sonnet_4,
            AnthropicModels.Sonnet_3_7,
            AnthropicModels.Sonnet_3_5,
            AnthropicModels.Opus_4,
            AnthropicModels.Opus_3,
            AnthropicModels.Haiku_3_5,
            AnthropicModels.Haiku_3,
        ).map { card -> AnthropicModel(card) }
    }

    private inner class AnthropicModel(
        private val delegate: LLModel,
    ) : Model {
        override val provider: ModelProvider get() = this@AnthropicModelProvider
        override val name: String get() = delegate.id
        override val size: Long? get() = null
        override val parameterCount: Long? get() = null
        override val contextLength: Long? get() = null
        override val quantizationLevel: String? get() = null
        override val supportsTools: Boolean get() = LLMCapability.Tools in delegate.capabilities

        override fun toLLModel(): LLModel = delegate
        override fun getLLMClient(): LLMClient = this@AnthropicModelProvider.client
    }
}
