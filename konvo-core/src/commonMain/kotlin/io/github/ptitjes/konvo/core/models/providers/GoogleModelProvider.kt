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

    override suspend fun query(): List<Model> {
        return listOf(
            GoogleModels.Gemini2_5Pro,
            GoogleModels.Gemini2_5Flash,
            GoogleModels.Gemini2_0Flash,
            GoogleModels.Gemini2_0FlashLite,
        ).map { card -> GoogleModel(card) }
    }

    private inner class GoogleModel(
        private val delegate: LLModel,
    ) : Model {
        override val provider: ModelProvider get() = this@GoogleModelProvider
        override val name: String get() = delegate.id
        override val size: Long? get() = null
        override val parameterCount: Long? get() = null
        override val contextLength: Long? get() = null
        override val quantizationLevel: String? get() = null
        override val supportsTools: Boolean get() = LLMCapability.Tools in delegate.capabilities

        override fun toLLModel(): LLModel = delegate
        override fun getLLMClient(): LLMClient = this@GoogleModelProvider.client
    }
}
