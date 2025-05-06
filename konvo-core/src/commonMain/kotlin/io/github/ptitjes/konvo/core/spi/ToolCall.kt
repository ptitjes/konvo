package io.github.ptitjes.konvo.core.spi

import kotlinx.serialization.json.*

data class ToolCall(
    val name: String,
    val arguments: Map<String, JsonElement>,
)
