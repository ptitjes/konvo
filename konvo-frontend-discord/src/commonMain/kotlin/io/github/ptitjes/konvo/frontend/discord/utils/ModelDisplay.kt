package io.github.ptitjes.konvo.frontend.discord.utils

import io.github.ptitjes.konvo.core.ai.spi.ModelCard
import kotlin.math.*

val ModelCard.shortName: String get() = name.removeSuffix(":latest")

val ModelCard.description: String
    get() = "Parameters: $parameterSize; Context: $contextLength; Size: $sizeString; Quantization: $quantizationLevel"

val ModelCard.sizeString: String get() = size?.let { it.humanReadableSize(1000) + "B" } ?: "?"
val ModelCard.contextLength: String get() = contextSize?.humanReadableSize(1024, 0) ?: "?"

private fun Long.humanReadableSize(base: Long, digitCount: Int = 1): String {
    val units = listOf("", "K", "M", "G", "T", "P", "E")
    val exponent = (ln(this.toDouble()) / ln(base.toDouble())).toInt()
    val unit = units[min(exponent, units.size - 1)]
    return String.format("%.${digitCount}f%s", this / base.toDouble().pow(exponent), unit)
}
