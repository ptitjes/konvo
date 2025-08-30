package io.github.ptitjes.konvo.core.models.providers

import ai.koog.prompt.executor.clients.*
import ai.koog.prompt.executor.clients.google.*
import ai.koog.prompt.llm.*
import io.github.ptitjes.konvo.core.models.*

class GoogleModelProvider(
    override val name: String,
    apiKey: String,
) : ModelProvider {
    private val client by lazy { GoogleLLMClient(apiKey) }

    override suspend fun queryModels(): List<ModelCard> {
        return listOf(
            GoogleModels.Gemini2_5Pro to "Gemini 2.5 Pro",
            GoogleModels.Gemini2_5Flash to "Gemini 2.5 Flash",
            GoogleModels.Gemini2_0Flash to "Gemini 2.0 Flash",
            GoogleModels.Gemini2_0FlashLite to "Gemini 2.0 Flash Lite",
        ).map { (model, name) -> GoogleModelCard(model, name) }
    }

    private inner class GoogleModelCard(
        private val delegate: LLModel,
        override val name: String,
    ) : ModelCard {
        override val provider: ModelProvider get() = this@GoogleModelProvider
        override val size: Long? get() = null
        override val parameterCount: Long? get() = null
        override val contextLength: Long? get() = null
        override val quantizationLevel: String? get() = null
        override val supportsTools: Boolean get() = LLMCapability.Tools in delegate.capabilities

        override fun toLLModel(): LLModel = delegate
        override fun getLLMClient(): LLMClient = this@GoogleModelProvider.client
    }
}
