package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.runtime.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.agents.toolkit.*
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
        addSingleton { new(::McpPromptProvider) }
    }

    bindSingleton { ToolPermissions(default = ToolPermission.ASK) }

    inBindSet<ToolProvider> {
        addSingleton { new(::McpToolProvider) }
    }

    bindSingleton { new(::FileSystemCharacterProvider) }
    bindSingleton { new(::FileSystemLorebookProvider) }

    inBindSet<CharacterProvider> {
        addSingleton { instance<FileSystemCharacterProvider>() }
    }

    inBindSet<LorebookProvider> {
        addSingleton { instance<FileSystemLorebookProvider>() }
    }

    bindSingleton<McpServerSpecificationsManager> {
        new(
            ::SettingsBasedMcpServerSpecificationsManager,
            coroutineContext
        )
    }
    bindSingleton<SettingsBasedModelManager> { new(::SettingsBasedModelManager, coroutineContext) }
    bindSingleton<ModelManager> { instance<SettingsBasedModelManager>() }

    bindSingleton<PromptManager> { new(::DiPromptManager, coroutineContext) }
    bindSingleton<ToolManager> { new(::DiToolManager, coroutineContext) }
    bindSingleton<CharacterManager> { new(::DiCharacterManager, coroutineContext) }
    bindSingleton<LorebookManager> { new(::DiLorebookManager, coroutineContext) }

    bindSingleton<(CoroutineContext) -> McpHostSession> {
        { coroutineContext: CoroutineContext -> new(::McpHostSession, coroutineContext) }
    }

    bindSingleton<SettingsRepository> { new(::FileSystemSettingsRepository) }

    bindSet<InteractiveAgent<*>>()

    inBindSet<InteractiveAgent<*>> {
        addSingleton { new(::QuestionAnswerAgent) }
        addSingleton { new(::RoleplayAgent) }
    }

    bindSingleton { new(::AgentFactory) }

//    bindSingletonOf<ConversationRepository>(::InMemoryConversationRepository)
    bindSingleton<ConversationRepository> { new(::FileConversationRepository) }

    bindSingleton { new(::ConversationManager, coroutineContext) }

    bindProviderOf(::SettingsListViewModel)
    bindProviderOf(::SettingsViewModel)
    bindProviderOf(::ConversationListViewModel)
    bindProviderOf(::NewConversationViewModel)
    bindProviderOf(::MainScreenViewModel)
    bindFactory { conversationId: String -> new(::ConversationViewModel, conversationId) }
}
