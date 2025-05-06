package io.github.ptitjes.konvo.core.spi

import kotlinx.serialization.json.*

data class ToolParameters(
    val properties: Map<String, JsonObject>,
    val required: List<String>,
)
