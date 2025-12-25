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

    override suspend fun queryModels(): List<ModelCard> {
        return listOf(
            OpenAIModels.Chat.GPT5_2 to "GPT-5.2",
            OpenAIModels.Chat.GPT5_1 to "GPT-5.1",
            OpenAIModels.Chat.GPT5 to "GPT-5",
            OpenAIModels.Chat.GPT5Mini to "GPT-5 Mini",
            OpenAIModels.Chat.GPT5Nano to "GPT-5 Nano",
            OpenAIModels.Chat.GPT4o to "GPT-4o",
            OpenAIModels.Chat.GPT4_1 to "GPT-4.1",
            OpenAIModels.Chat.GPT4_1Mini to "GPT-4.1 Mini",
            OpenAIModels.Chat.GPT4_1Nano to "GPT-4.1 Nano",
            OpenAIModels.Chat.O4Mini to "o4 Mini",
            OpenAIModels.Chat.O3 to "o3",
            OpenAIModels.Chat.O3Mini to "o3 Mini",
            OpenAIModels.Chat.O1 to "o1",
        ).map { (model, name) -> OpenAIModelCard(model, name) }
    }

    private inner class OpenAIModelCard(
        private val delegate: LLModel,
        override val name: String,
    ) : ModelCard {
        override val provider: ModelProvider get() = this@OpenAIModelProvider
        override val size: Long? get() = null
        override val parameterCount: Long? get() = null
        override val contextLength: Long? get() = null
        override val quantizationLevel: String? get() = null
        override val supportsTools: Boolean get() = LLMCapability.Tools in delegate.capabilities

        override fun toLLModel(): LLModel = delegate
        override fun getLLMClient(): LLMClient = this@OpenAIModelProvider.client
    }
}
