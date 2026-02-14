package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.runtime.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.storage.*
import io.github.ptitjes.konvo.core.conversations.storage.files.*
import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.platform.*
import io.github.ptitjes.konvo.core.prompts.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.core.roleplay.providers.*
import io.github.ptitjes.konvo.core.settings.*
import io.github.ptitjes.konvo.core.tools.*
import io.github.ptitjes.konvo.frontend.compose.conversations.*
import io.github.ptitjes.konvo.frontend.compose.resources.*
import io.github.ptitjes.konvo.frontend.compose.settings.*
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.*
import org.kodein.di.*
import org.kodein.di.compose.*
import kotlin.coroutines.*

fun runComposeFrontend() = application {
    var di by remember { mutableStateOf<DI?>(null) }

    LaunchedEffect(Unit) {
        di = buildDi()
        // We need a slight delay, otherwise the recomposition is cancelled
        delay(100)
    }

    di?.let {
        withDI(it) {
            Window(
                title = "Konvo",
                state = rememberWindowState(width = 375.dp, height = 667.dp),
//                state = rememberWindowState(width = 1280.dp, height = 720.dp),
                icon = painterResource(Res.drawable.ic_icon),
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
                val containerSize = LocalWindowInfo.current.containerSize
                if (containerSize != IntSize(0, 0)) {
                    App()
                }
            }
        }
    }
}

fun CoroutineScope.buildDi() = DI {
    bindSet<PromptProvider>()
    bindSet<ToolProvider>()
    bindSet<CharacterProvider>()
    bindSet<LorebookProvider>()

    bindSingleton<StoragePaths> { DesktopHomeStoragePaths() }

    inBindSet<PromptProvider> {
        add { singleton { McpPromptProvider(instance()) } }
    }

    bindSingleton { ToolPermissions(default = ToolPermission.ASK) }

    inBindSet<ToolProvider> {
        add { singleton { McpToolProvider(instance(), permissions = instance()) } }
    }

    bindSingletonOf(::FileSystemCharacterProvider)
    bindSingletonOf(::FileSystemLorebookProvider)

    inBindSet<CharacterProvider> {
        add { singleton { instance<FileSystemCharacterProvider>() } }
    }

    inBindSet<LorebookProvider> {
        add { singleton { instance<FileSystemLorebookProvider>() } }
    }

    bind<McpServerSpecificationsManager> {
        singleton {
            SettingsBasedMcpServerSpecificationsManager(coroutineContext, instance())
        }
    }

    bind<SettingsBasedModelManager> { singleton { SettingsBasedModelManager(coroutineContext, instance()) } }
    bind<ModelManager> { singleton { instance<SettingsBasedModelManager>() } }

    bind<PromptManager> { singleton { DiPromptManager(coroutineContext, instance()) } }
    bind<ToolManager> { singleton { DiToolManager(coroutineContext, instance()) } }
    bind<CharacterManager> { singleton { DiCharacterManager(coroutineContext, instance()) } }
    bind<LorebookManager> { singleton { DiLorebookManager(coroutineContext, instance()) } }

    bindFactory<CoroutineContext, McpHostSession> { coroutineContext: CoroutineContext ->
        McpHostSession(coroutineContext, instance(), instance())
    }

    bind<SettingsRepository> { singleton { FileSystemSettingsRepository(instance()) } }

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

    bindSingleton { ConversationManager(coroutineContext, instance(), instance()) }

    bindProviderOf(::SettingsListViewModel)
    bindProviderOf(::SettingsViewModel)
    bindProviderOf(::ConversationListViewModel)
    bindProviderOf(::NewConversationViewModel)
    bindProviderOf(::MainScreenViewModel)
    bindFactory { conversationId: String ->
        ConversationViewModel(instance(), instance(), conversationId)
    }
}
