package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.runtime.*
import androidx.compose.ui.window.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.koog.*
import io.github.ptitjes.konvo.core.ai.mcp.*
import kotlinx.coroutines.*
import kotlinx.io.files.*

fun runComposeFrontend() = application {
    var konvo by remember { mutableStateOf<Konvo?>(null) }

    LaunchedEffect(Unit) {
        val configuration = KonvoAppConfiguration.readConfiguration(Path("config/konvo.json5"))

        val mcpServersManager = McpServersManager(
            coroutineContext = coroutineContext + Dispatchers.Default,
            specifications = configuration.mcp.servers,
        ).apply { startAndConnectServers() }

        konvo = Konvo {
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
        }.apply { init() }
    }

    konvo?.let { konvo ->
        Window(
            title = "Konvo",
            onCloseRequest = ::exitApplication,
        ) {
            App(konvo)
        }
    }
}
