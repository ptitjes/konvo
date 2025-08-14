package io.github.ptitjes.konvo

import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.*
import io.github.ptitjes.konvo.core.ai.characters.*
import io.github.ptitjes.konvo.core.ai.koog.*
import io.github.ptitjes.konvo.core.ai.mcp.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.base.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import io.github.ptitjes.konvo.core.conversation.storage.files.*
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
        di.direct.instance<ProviderManager<ModelCard>>().init()
        di.direct.instance<ProviderManager<PromptCard>>().init()
        di.direct.instance<ProviderManager<ToolCard>>().init()
        di.direct.instance<ProviderManager<CharacterCard>>().init()

        discordBot(konvo, configuration.discord.token)
    } finally {
        mcpServersManager.disconnectAndStopServers()
    }
}

fun CoroutineScope.buildDi(configuration: KonvoAppConfiguration) = DI {
    bindSet<Provider<ModelCard>>()
    bindSet<Provider<PromptCard>>()
    bindSet<Provider<ToolCard>>()
    bindSet<Provider<CharacterCard>>()

    bindSingleton<StoragePaths> { LinuxXdgServerStoragePaths() }

    import(configurationProviders(configuration))

    bind<ProviderManager<ModelCard>> { singleton { DiProviderManager(instance()) } }
    bind<ProviderManager<PromptCard>> { singleton { DiProviderManager(instance()) } }
    bind<ProviderManager<ToolCard>> { singleton { DiProviderManager(instance()) } }
    bind<ProviderManager<CharacterCard>> { singleton { DiProviderManager(instance()) } }

    bindSingleton<Konvo> { Konvo(di) }

//    bindSingletonOf<ConversationRepository>(::InMemoryConversationRepository)
    bindSingleton<ConversationRepository> {
        FileConversationRepository(
            storagePaths = instance(),
            konvo = instance(),
        )
    }

    bindSingleton { LiveConversationsManager(coroutineContext, instance()) }
}

fun CoroutineScope.configurationProviders(configuration: KonvoAppConfiguration) = DI.Module("providers") {
    bindConstant(tag = DataDirectory) { configuration.dataDirectory }

    inBindSet<Provider<ModelCard>> {
        configuration.modelProviders.forEach { (name, configuration) ->
            add { singleton { configuration.buildModelProvider(name) } }
        }
    }

    bind { singleton { McpServersManager(coroutineContext, configuration.mcp.servers) } }

    inBindSet<Provider<PromptCard>> {
        add { singleton { McpPromptProvider(instance()) } }
    }

    inBindSet<Provider<ToolCard>> {
        add { singleton { McpToolProvider(instance(), configuration.mcp.toolPermissions) } }
    }

    inBindSet<Provider<CharacterCard>> {
        add { singleton { FileSystemCharacterProvider(instance()) } }
    }
}

private fun ModelProviderConfiguration.buildModelProvider(name: String): Provider<ModelCard> = when (this) {
    is ModelProviderConfiguration.Ollama -> OllamaModelProvider(name, this.baseUrl)
}

object DataDirectory
