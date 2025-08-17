package io.github.ptitjes.konvo

import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.*
import io.github.ptitjes.konvo.core.ai.mcp.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.characters.providers.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import io.github.ptitjes.konvo.core.conversation.storage.files.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.models.providers.*
import io.github.ptitjes.konvo.core.platform.*
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
        di.direct.instance<ModelManager>().init()
        di.direct.instance<ProviderManager<PromptCard>>().init()
        di.direct.instance<ProviderManager<ToolCard>>().init()
        di.direct.instance<CharacterCardManager>().init()

        discordBot(konvo, configuration.discord.token)
    } finally {
        mcpServersManager.disconnectAndStopServers()
    }
}

fun CoroutineScope.buildDi(configuration: KonvoAppConfiguration) = DI {
    bindSet<ModelProvider>()
    bindSet<Provider<PromptCard>>()
    bindSet<Provider<ToolCard>>()
    bindSet<CharacterCardProvider>()

    bindSingleton<StoragePaths> { LinuxXdgServerStoragePaths() }

    import(configurationProviders(configuration))

    bind<ModelManager> { singleton { DiModelManager(instance()) } }
    bind<ProviderManager<PromptCard>> { singleton { DiProviderManager(instance()) } }
    bind<ProviderManager<ToolCard>> { singleton { DiProviderManager(instance()) } }
    bind<CharacterCardManager> { singleton { DiCharacterCardManager(instance()) } }

    bindSingleton<Konvo> { Konvo(di) }

//    bindSingletonOf<ConversationRepository>(::InMemoryConversationRepository)
    bindSingleton<ConversationRepository> {
        FileConversationRepository(
            storagePaths = instance(),
            konvo = instance(),
        )
    }

    bindSingleton { LiveConversationsManager(coroutineContext, instance(), instance()) }
}

fun CoroutineScope.configurationProviders(configuration: KonvoAppConfiguration) = DI.Module("providers") {
    bindConstant(tag = DataDirectory) { configuration.dataDirectory }

    inBindSet<ModelProvider> {
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

    inBindSet<CharacterCardProvider> {
        add { singleton { FileSystemCharacterCardProvider(instance()) } }
    }
}

private fun ModelProviderConfiguration.buildModelProvider(name: String): ModelProvider = when (this) {
    is ModelProviderConfiguration.Ollama -> OllamaModelProvider(name, this.baseUrl)
    is ModelProviderConfiguration.Anthropic -> AnthropicModelProvider(name, this.apiKey)
    is ModelProviderConfiguration.OpenAI -> OpenAIModelProvider(name, this.apiKey)
    is ModelProviderConfiguration.Google -> GoogleModelProvider(name, this.apiKey)
}

object DataDirectory
