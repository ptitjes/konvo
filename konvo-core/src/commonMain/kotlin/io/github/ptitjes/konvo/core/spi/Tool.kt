package io.github.ptitjes.konvo.core.spi

import kotlinx.serialization.json.*

data class Tool(
    val name: String,
    val description: String,
    val parameters: ToolParameters,
    val askPermission: Boolean,
    val evaluator: suspend (Map<String, JsonElement>) -> String,
)
