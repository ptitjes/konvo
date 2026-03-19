package io.github.ptitjes.konvo.plugin.core.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.navigation3.runtime.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.viewmodels.*

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(viewModel.backStack) {
        println("Back stack changed: ${viewModel.backStack.joinToString(", ")}")
    }

    val navigator = viewModel.navigator

    MainScreenScaffold(
        modifier = modifier,
        backStack = navigator.backStack,
        onBack = { navigator.navigateBack() },
        navigationPaneState = navigator.navigationPaneState,
        extraPaneState = navigator.extraPaneState,
        entryProvider = entryProvider {

            entry<Destination.Conversation.List>(metadata = CenterStageScene.navigation()) {
                ConversationListScreen(navigator = navigator)
            }

            entry<Destination.Conversation.New>(metadata = CenterStageScene.content()) {
                AgentConfigurationScreen(navigator = navigator)
            }

            entry<Destination.Conversation.Selected>(
                metadata = CenterStageScene.content(
                    defaultExtraContent = {
                        val containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        val contentColor = MaterialTheme.colorScheme.onSurface
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = containerColor,
                            contentColor = contentColor,
                        ) {

                        }
                    }
                )
            ) {
                ConversationScreen(conversationId = it.id, navigator = navigator)
            }
        }
    )

    SettingsWindow(navigator = viewModel.settingsNavigator)
}
