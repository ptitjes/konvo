package io.github.ptitjes.konvo

import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.characters.*
import io.github.ptitjes.konvo.core.ai.koog.*
import io.github.ptitjes.konvo.core.ai.mcp.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.frontend.discord.*
import kotlinx.coroutines.*
import kotlinx.io.files.*
import org.kodein.di.*

suspend fun main() = coroutineScope {
    val configuration = KonvoAppConfiguration.readConfiguration(Path("config/konvo.json5"))

    val di = buildDi(configuration)

    val mcpServersManager = di.direct.instance<McpServersManager>()
    val konvo = di.direct.instance<Konvo>()

    try {
        mcpServersManager.startAndConnectServers()
        konvo.init()

        konvo.discordBot(configuration.discord.token)
    } finally {
        mcpServersManager.disconnectAndStopServers()
    }
}

fun CoroutineScope.buildDi(configuration: KonvoAppConfiguration) = DI {
    bindSet<ModelProvider>()
    bindSet<PromptProvider>()
    bindSet<ToolProvider>()
    bindSet<CharacterProvider>()

    import(configurationProviders(configuration))

    bindSingleton<Konvo> { Konvo(coroutineContext, di) }
}

fun CoroutineScope.configurationProviders(configuration: KonvoAppConfiguration) = DI.Module("providers") {
    bindConstant(tag = DataDirectory) { configuration.dataDirectory }

    inBindSet<ModelProvider> {
        configuration.modelProviders.forEach { (name, configuration) ->
            add { singleton { configuration.buildModelProvider(name) } }
        }
    }

    bind { singleton { McpServersManager(coroutineContext, configuration.mcp.servers) } }

    inBindSet<PromptProvider> {
        add { singleton { McpPromptProvider(instance()) } }
    }

    inBindSet<ToolProvider> {
        add { singleton { McpToolProvider(instance(), configuration.mcp.toolPermissions) } }
    }

    inBindSet<CharacterProvider> {
        add { singleton { FileSystemCharacterProvider(instance(tag = DataDirectory)) } }
    }
}

private fun ModelProviderConfiguration.buildModelProvider(name: String): OllamaModelProvider =
    when (this) {
        is ModelProviderConfiguration.Ollama -> OllamaModelProvider(name, this.baseUrl)
    }

object DataDirectory
