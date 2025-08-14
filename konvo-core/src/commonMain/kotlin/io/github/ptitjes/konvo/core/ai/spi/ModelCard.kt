package io.github.ptitjes.konvo.core.ai.spi

import ai.koog.prompt.executor.clients.*
import ai.koog.prompt.llm.*

interface ModelCard {
    val provider: Provider<ModelCard>
    val name: String
    val size: Long?
    val parameterCount: Long?
    val contextLength: Long?
    val quantizationLevel: String?
    val supportsTools: Boolean

    fun toLLModel(): LLModel

    fun getLLMClient(): LLMClient
}
