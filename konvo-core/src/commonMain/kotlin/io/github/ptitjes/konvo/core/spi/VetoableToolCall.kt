package io.github.ptitjes.konvo.core.spi

import kotlinx.serialization.json.*

interface VetoableToolCall {
    val tool: Tool
    val arguments: Map<String, JsonElement>

    fun allow()
    fun reject()
}
