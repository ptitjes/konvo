package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.frontend.compose.screens.*
import io.github.ptitjes.konvo.frontend.compose.theme.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*

@Composable
fun App(
    konvo: Konvo,
) {
    KonvoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.NewConversation) }

            when (val screen = currentScreen) {
                Screen.NewConversation -> {
                    NewConversationScreen(
                        konvo = konvo,
                        onConversationCreated = { newConversation ->
                            currentScreen = Screen.InConversation(newConversation)
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is Screen.InConversation -> {
                    val viewModel = remember(screen.conversation) {
                        val conversationView = screen.conversation.newUiView()
                        ConversationViewModel(conversationView)
                    }
                    ConversationScreen(
                        viewModel = viewModel,
                        onBackClick = { currentScreen = Screen.NewConversation },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

private sealed class Screen {
    data object NewConversation : Screen()

    data class InConversation(
        val conversation: ActiveConversation,
    ) : Screen()
}
