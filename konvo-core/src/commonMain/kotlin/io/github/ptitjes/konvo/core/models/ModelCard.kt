package io.github.ptitjes.konvo.core.models

import ai.koog.prompt.executor.clients.*
import ai.koog.prompt.llm.*

interface ModelCard {
    val name: String
    val size: Long?
    val parameterCount: Long?
    val contextLength: Long?
    val quantizationLevel: String?
    val supportsTools: Boolean
    val provider: ModelProvider

    fun toLLModel(): LLModel

    fun getLLMClient(): LLMClient
}
