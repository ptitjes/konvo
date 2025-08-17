package io.github.ptitjes.konvo.core.ai.spi

import ai.koog.prompt.executor.clients.*
import ai.koog.prompt.llm.*

interface ModelCard : NamedCard {
    override val name: String
    val size: Long?
    val parameterCount: Long?
    val contextLength: Long?
    val quantizationLevel: String?
    val supportsTools: Boolean
    val provider: Provider<ModelCard>

    fun toLLModel(): LLModel

    fun getLLMClient(): LLMClient
}
