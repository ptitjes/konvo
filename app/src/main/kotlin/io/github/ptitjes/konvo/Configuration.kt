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
    val modelProviders: Map<String, ModelProviderConfiguration>,
    val discord: DiscordConfiguration,
    val mcp: McpConfiguration,
) {
    companion object {
        fun readConfiguration(path: Path): KonvoAppConfiguration =
            defaultFileSystem.source(path).buffered().use { source ->
                configurationJson.decodeFromString(source.readString())
            }

        @OptIn(ExperimentalSerializationApi::class)
        private val configurationJson = Json {
            isLenient = true
            allowComments = true
            allowTrailingComma = true
        }
    }
}

@Serializable
sealed interface ModelProviderConfiguration {

    @Serializable
    @SerialName(value = "ollama")
    data class Ollama(val baseUrl: String) : ModelProviderConfiguration
}

@Serializable
data class DiscordConfiguration(
    val token: String,
)
