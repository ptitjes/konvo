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
    val llms: List<LlmClientConfiguration>,
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
sealed interface LlmClientConfiguration {

    @Serializable
    @SerialName(value = "ollama")
    data class Ollama(
        val name: String,
        val url: String,
    ) : LlmClientConfiguration
}
