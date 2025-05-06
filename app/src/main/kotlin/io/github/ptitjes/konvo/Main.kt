package io.github.ptitjes.konvo

import io.github.ptitjes.konvo.backend.mcp.*
import io.github.ptitjes.konvo.backend.ollama.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.frontend.discord.*
import kotlinx.io.files.*

suspend fun main() {
    val configuration = KonvoAppConfiguration.readConfiguration(Path("konvo.json"))

    val mcpServersManager = McpServersManager(
        specifications = configuration.mcp.servers,
    )

    try {
        mcpServersManager.startAndConnectServers()

        val konvo = startKonvo {
            dataDirectory = configuration.dataDirectory

            installModels(OllamaProvider(configuration.ollama.url))

            installTools(
                McpToolProvider(
                    serversManager = mcpServersManager,
                    permissions = configuration.mcp.toolPermissions,
                )
            )
        }

        konvo.discordBot(configuration.discord.token)
    } finally {
        mcpServersManager.closeServers()
    }
}
