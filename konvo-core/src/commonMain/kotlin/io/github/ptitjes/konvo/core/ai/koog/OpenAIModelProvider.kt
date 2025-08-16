package io.github.ptitjes.konvo.core.ai.koog

import ai.koog.prompt.executor.clients.*
import ai.koog.prompt.executor.clients.openai.*
import ai.koog.prompt.llm.*
import io.github.ptitjes.konvo.core.ai.spi.*

class OpenAIModelProvider(
    override val name: String,
    apiKey: String,
) : Provider<ModelCard> {
    private val client by lazy { OpenAILLMClient(apiKey) }

    override suspend fun query(): List<ModelCard> {
        return listOf(
            OpenAIModels.Chat.GPT4o,
            OpenAIModels.Reasoning.GPT4oMini,
            OpenAIModels.Chat.GPT4_1,
            OpenAIModels.CostOptimized.GPT4_1Mini,
            OpenAIModels.CostOptimized.GPT4_1Nano,
            OpenAIModels.Reasoning.O3,
            OpenAIModels.Reasoning.O3Mini,
            OpenAIModels.Reasoning.O1,
            OpenAIModels.Reasoning.O1Mini,
        ).map { card -> OpenAIModelCard(card) }
    }

    private inner class OpenAIModelCard(
        private val delegate: LLModel,
    ) : ModelCard {
        override val provider: Provider<ModelCard> get() = this@OpenAIModelProvider
        override val name: String get() = delegate.id
        override val size: Long? get() = null
        override val parameterCount: Long? get() = null
        override val contextLength: Long? get() = null
        override val quantizationLevel: String? get() = null
        override val supportsTools: Boolean get() = LLMCapability.Tools in delegate.capabilities

        override fun toLLModel(): LLModel = delegate
        override fun getLLMClient(): LLMClient = this@OpenAIModelProvider.client
    }
}
