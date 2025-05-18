package io.github.ptitjes.konvo.core.ai.spi

data class ModelCard(
    val provider: ModelProvider,
    val name: String,
    val size: Long? = null,
    val parameterSize: String? = null,
    val quantizationLevel: String? = null,
    val contextSize: Long? = null,
    val supportsTools: Boolean = false,
)
