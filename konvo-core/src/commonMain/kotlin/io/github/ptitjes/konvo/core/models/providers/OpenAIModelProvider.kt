package io.github.ptitjes.konvo.core.models.providers

import ai.koog.prompt.executor.clients.*
import ai.koog.prompt.executor.clients.openai.*
import ai.koog.prompt.llm.*
import io.github.ptitjes.konvo.core.models.*

class OpenAIModelProvider(
    override val name: String,
    apiKey: String,
) : ModelProvider {
    private val client by lazy { OpenAILLMClient(apiKey) }

    override suspend fun query(): List<Model> {
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
        ).map { card -> OpenAIModel(card) }
    }

    private inner class OpenAIModel(
        private val delegate: LLModel,
    ) : Model {
        override val provider: ModelProvider get() = this@OpenAIModelProvider
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
