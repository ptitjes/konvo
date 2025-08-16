package io.github.ptitjes.konvo.core.ai.koog

import ai.koog.prompt.executor.clients.*
import ai.koog.prompt.executor.clients.anthropic.*
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.llm.*
import io.github.ptitjes.konvo.core.ai.spi.*

class GoogleModelProvider(
    override val name: String,
    apiKey: String,
) : Provider<ModelCard> {
    private val client by lazy { GoogleLLMClient(apiKey) }

    override suspend fun query(): List<ModelCard> {
        return listOf(
            GoogleModels.Gemini2_5Pro,
            GoogleModels.Gemini2_5Flash,
            GoogleModels.Gemini2_0Flash,
            GoogleModels.Gemini2_0FlashLite,
        ).map { card -> GoogleModelCard(card) }
    }

    private inner class GoogleModelCard(
        private val delegate: LLModel,
    ) : ModelCard {
        override val provider: Provider<ModelCard> get() = this@GoogleModelProvider
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
