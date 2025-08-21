package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.runtime.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import io.github.ptitjes.konvo.core.conversation.storage.files.*
import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.platform.*
import io.github.ptitjes.konvo.core.prompts.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.core.roleplay.providers.*
import io.github.ptitjes.konvo.core.settings.*
import io.github.ptitjes.konvo.core.tools.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*
import kotlinx.coroutines.*
import kotlinx.io.files.*
import org.kodein.di.*
import org.kodein.di.compose.*
import kotlin.coroutines.*

fun runComposeFrontend() = application {
    var di by remember { mutableStateOf<DI?>(null) }

    LaunchedEffect(Unit) {
        val configuration = KonvoAppConfiguration.readConfiguration(Path("config/konvo.json5"))
        di = buildDi(configuration)
        // We need a slight delay, otherwise the recomposition is cancelled
        delay(100)
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
    bindSet<PromptProvider>()
    bindSet<ToolProvider>()
    bindSet<CharacterProvider>()
    bindSet<LorebookProvider>()

    bindSingleton<StoragePaths> { LinuxXdgHomeStoragePaths() }

    import(configurationProviders(configuration))

    bind<McpServerSpecificationsManager> {
        singleton {
            SettingsBasedMcpServerSpecificationsManager(coroutineContext, instance())
        }
    }

    bind<ModelManager> { singleton { SettingsBasedModelProviderManager(coroutineContext, instance()) } }
    bind<PromptManager> { singleton { DiPromptManager(coroutineContext, instance()) } }
    bind<ToolManager> { singleton { DiToolManager(coroutineContext, instance()) } }
    bind<CharacterManager> { singleton { DiCharacterManager(coroutineContext, instance()) } }
    bind<LorebookManager> { singleton { DiLorebookManager(coroutineContext, instance()) } }

    bindFactory<CoroutineContext, McpHostSession> { coroutineContext: CoroutineContext ->
        McpHostSession(coroutineContext, instance())
    }

    bind<SettingsRepository> { singleton { FileSystemSettingsRepository(instance()) } }

    bind { singleton { SettingsListViewModel() } }
    bind { singleton { SettingsViewModel(instance()) } }

    bind {
        singleton {
            AgentFactory(
                modelProviderManager = instance(),
                mcpSessionFactory = factory(),
                characterProviderManager = instance(),
                settingsRepository = instance(),
                lorebookManager = instance(),
            )
        }
    }

//    bindSingletonOf<ConversationRepository>(::InMemoryConversationRepository)
    bindSingleton<ConversationRepository> {
        FileConversationRepository(
            storagePaths = instance(),
        )
    }

    bindSingleton { LiveConversationsManager(coroutineContext, instance(), instance()) }

    bindSingletonOf(::ConversationListViewModel)
    bind {
        singleton {
            NewConversationViewModel(
                modelManager = instance(),
                characterManager = instance(),
                lorebookManager = instance(),
                mcpServerSpecificationsManager = instance(),
                conversationRepository = instance(),
                settingsRepository = instance(),
            )
        }
    }
    bindSingletonOf(::AppViewModel)
    bindFactory { initialConversation: Conversation ->
        ConversationViewModel(instance(), instance(), initialConversation)
    }
}

fun CoroutineScope.configurationProviders(configuration: KonvoAppConfiguration) = DI.Module("providers") {
    bindConstant(tag = DataDirectory) { configuration.dataDirectory }

    inBindSet<PromptProvider> {
        add { singleton { McpPromptProvider(instance()) } }
    }

    inBindSet<ToolProvider> {
        add { singleton { McpToolProvider(instance(), configuration.mcp.toolPermissions) } }
    }

    inBindSet<CharacterProvider> {
        add { singleton { FileSystemCharacterProvider(instance()) } }
    }

    inBindSet<LorebookProvider> {
        add { singleton { FileSystemLorebookProvider(instance()) } }
    }
}

object DataDirectory
