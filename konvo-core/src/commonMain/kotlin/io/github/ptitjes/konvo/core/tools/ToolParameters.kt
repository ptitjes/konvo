package io.github.ptitjes.konvo.core.tools

import kotlinx.serialization.json.*

data class ToolParameters(
    val properties: Map<String, JsonObject>,
    val required: List<String>,
)
