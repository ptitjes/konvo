package io.github.ptitjes.konvo

import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.mcp.*
import kotlinx.io.*
import kotlinx.io.files.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class KonvoAppConfiguration(
    val dataDirectory: String,
    val ollama: OllamaConfiguration,
    val discord: DiscordConfiguration,
    val mcp: McpConfiguration,
) {
    companion object {
        fun readConfiguration(path: Path): KonvoAppConfiguration =
            defaultFileSystem.source(path).buffered().use { source ->
                Json.decodeFromString(source.readString())
            }
    }
}

@Serializable
data class DiscordConfiguration(
    val token: String,
)

@Serializable
data class OllamaConfiguration(
    val url: String,
)
