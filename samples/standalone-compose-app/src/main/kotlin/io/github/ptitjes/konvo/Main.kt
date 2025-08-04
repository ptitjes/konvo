package io.github.ptitjes.konvo

import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.koog.*
import io.github.ptitjes.konvo.core.ai.mcp.*
import io.github.ptitjes.konvo.frontend.compose.*
import kotlinx.coroutines.*
import kotlinx.io.files.*

suspend fun main() = coroutineScope {
    val configuration = KonvoAppConfiguration.readConfiguration(Path("config/konvo.json5"))

    val mcpServersManager = McpServersManager(
        coroutineContext = coroutineContext,
        specifications = configuration.mcp.servers,
    )

    try {
        mcpServersManager.startAndConnectServers()

        val konvo = startKonvo {
            dataDirectory = configuration.dataDirectory

            configuration.modelProviders.forEach { (name, configuration) ->
                installModels(
                    when (configuration) {
                        is ModelProviderConfiguration.Ollama -> OllamaModelProvider(name, configuration.baseUrl)
                    }
                )
            }

            installPrompts(
                McpPromptProvider(
                    serversManager = mcpServersManager,
                )
            )

            installTools(
                McpToolProvider(
                    serversManager = mcpServersManager,
                    permissions = configuration.mcp.toolPermissions,
                )
            )
        }

        konvo.composeFrontend()
    } finally {
        mcpServersManager.disconnectAndStopServers()
    }
}
