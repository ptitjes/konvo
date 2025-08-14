package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.runtime.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.characters.*
import io.github.ptitjes.konvo.core.ai.koog.*
import io.github.ptitjes.konvo.core.ai.mcp.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import io.github.ptitjes.konvo.core.conversation.storage.files.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*
import kotlinx.coroutines.*
import kotlinx.io.files.*
import org.kodein.di.*
import org.kodein.di.compose.*
import io.github.ptitjes.konvo.core.base.*

fun runComposeFrontend() = application {
    var di by remember { mutableStateOf<DI?>(null) }

    LaunchedEffect(Unit) {
        val configuration = KonvoAppConfiguration.readConfiguration(Path("config/konvo.json5"))

        di = buildDi(configuration).apply {
            direct.instance<McpServersManager>().startAndConnectServers()
            direct.instance<Konvo>().init()
        }
    }

    di?.let {
        withDI(it) {
            Window(
                title = "Konvo",
                state = rememberWindowState(width = 1280.dp, height = 720.dp),
                onCloseRequest = ::exitApplication,
            ) {
                App()
            }
        }
    }
}

fun CoroutineScope.buildDi(configuration: KonvoAppConfiguration) = DI {
    bindSet<ModelProvider>()
    bindSet<PromptProvider>()
    bindSet<ToolProvider>()
    bindSet<CharacterProvider>()

    bindSingleton<StoragePaths> { LinuxXdgHomeStoragePaths() }

    import(configurationProviders(configuration))

    bindSingleton<Konvo> { Konvo(coroutineContext, di) }

//    bindSingletonOf<ConversationRepository>(::InMemoryConversationRepository)
    bindSingleton<ConversationRepository> {
        FileConversationRepository(
            rootPath = Path(configuration.dataDirectory, "conversations"),
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

private fun ModelProviderConfiguration.buildModelProvider(name: String): OllamaModelProvider = when (this) {
    is ModelProviderConfiguration.Ollama -> OllamaModelProvider(name, this.baseUrl)
}

object DataDirectory
