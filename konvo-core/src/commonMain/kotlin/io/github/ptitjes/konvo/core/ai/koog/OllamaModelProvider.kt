package io.github.ptitjes.konvo.core.ai.koog

import ai.koog.prompt.executor.clients.*
import ai.koog.prompt.executor.ollama.client.*
import ai.koog.prompt.llm.*
import io.github.ptitjes.konvo.core.ai.spi.*
import ai.koog.prompt.executor.ollama.client.OllamaModelCard as KoogOllamaModelCard

class OllamaModelProvider(
    urlString: String = "http://localhost:11434",
) : ModelProvider {
    override val name: String get() = "Koog"

    private val client = OllamaClient(urlString)

    override suspend fun queryModelCards(): List<ModelCard> {
        return client.getModels().map { card -> OllamaModelCard(card) }
    }

    private inner class OllamaModelCard(
        private val delegate: KoogOllamaModelCard,
    ) : ModelCard {
        override val provider: ModelProvider get() = this@OllamaModelProvider
        override val name: String get() = delegate.name
        override val size: Long get() = delegate.size
        override val parameterCount: Long? get() = delegate.parameterCount
        override val contextLength: Long? get() = delegate.contextLength
        override val quantizationLevel: String? get() = delegate.quantizationLevel
        override val supportsTools: Boolean get() = LLMCapability.Tools in delegate.capabilities

        override fun toLLModel(): LLModel = delegate.toLLModel()
        override fun getLLMClient(): LLMClient = this@OllamaModelProvider.client
    }
}
