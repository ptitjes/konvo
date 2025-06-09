package io.github.ptitjes.konvo

import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.koog.*
import io.github.ptitjes.konvo.core.ai.mcp.*
import io.github.ptitjes.konvo.frontend.discord.*
import kotlinx.coroutines.*
import kotlinx.io.files.*

suspend fun main() = coroutineScope {
    val configuration = KonvoAppConfiguration.readConfiguration(Path("config/konvo.json"))

    val mcpServersManager = McpServersManager(
        coroutineContext = coroutineContext,
        specifications = configuration.mcp.servers,
    )

    try {
        mcpServersManager.startAndConnectServers()

        val konvo = startKonvo {
            dataDirectory = configuration.dataDirectory

            configuration.llms.forEach { llm ->
                installModels(
                    when (llm) {
                        is LlmClientConfiguration.Ollama -> OllamaModelProvider(llm.url)
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

        konvo.discordBot(configuration.discord.token)
    } finally {
        mcpServersManager.disconnectAndStopServers()
    }
}
