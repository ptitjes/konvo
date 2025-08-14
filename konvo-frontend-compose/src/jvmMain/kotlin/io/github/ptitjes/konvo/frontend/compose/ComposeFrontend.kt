package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.runtime.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.*
import io.github.ptitjes.konvo.core.ai.characters.*
import io.github.ptitjes.konvo.core.ai.koog.*
import io.github.ptitjes.konvo.core.ai.mcp.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.base.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import io.github.ptitjes.konvo.core.conversation.storage.files.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*
import kotlinx.coroutines.*
import kotlinx.io.files.*
import org.kodein.di.*
import org.kodein.di.compose.*

fun runComposeFrontend() = application {
    var di by remember { mutableStateOf<DI?>(null) }

    LaunchedEffect(Unit) {
        val configuration = KonvoAppConfiguration.readConfiguration(Path("config/konvo.json5"))

        di = buildDi(configuration).apply {
            direct.instance<McpServersManager>().startAndConnectServers()
            direct.instance<ProviderManager<ModelCard>>().init()
            direct.instance<ProviderManager<PromptCard>>().init()
            direct.instance<ProviderManager<ToolCard>>().init()
            direct.instance<ProviderManager<CharacterCard>>().init()
        }
    }

    di?.let {
        withDI(it) {
            Window(
                title = "Konvo",
                state = rememberWindowState(width = 1280.dp, height = 720.dp),
                onCloseRequest = ::exitApplication,
                onKeyEvent = { event ->
                    if (event.type == KeyEventType.KeyUp && event.isCtrlPressed && event.key == Key.Q) {
                        exitApplication()
                        true
                    } else {
                        false
                    }
                },
            ) {
                App()
            }
        }
    }
}

fun CoroutineScope.buildDi(configuration: KonvoAppConfiguration) = DI {
    bindSet<Provider<ModelCard>>()
    bindSet<Provider<PromptCard>>()
    bindSet<Provider<ToolCard>>()
    bindSet<Provider<CharacterCard>>()

    bindSingleton<StoragePaths> { LinuxXdgHomeStoragePaths() }

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

    bindSingletonOf(::ConversationListViewModel)
    bindSingletonOf(::NewConversationViewModel)
    bindSingletonOf(::AppViewModel)
    bindFactory { initialConversation: Conversation ->
        ConversationViewModel(instance(), instance(), initialConversation)
    }
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
    is ModelProviderConfiguration.Anthropic -> AnthropicModelProvider(name, this.apiKey)
}

object DataDirectory
