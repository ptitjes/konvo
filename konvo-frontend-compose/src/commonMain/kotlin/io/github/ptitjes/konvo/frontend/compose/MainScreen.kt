package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.navigation3.runtime.*
import io.github.ptitjes.konvo.frontend.compose.conversations.*
import io.github.ptitjes.konvo.frontend.compose.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels.*

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
                NewConversationScreen(navigator = navigator)
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

            settingEntries(navigator)
        }
    )
}

private fun EntryProviderScope<Destination>.settingEntries(navigator: Navigator) {
    entry<Destination.Setting.List>(
//        metadata = SettingsDialogSceneStrategy.listPane(),
    ) {
        SettingsListScreen(
            navigator = navigator,
        )
    }
    entry<Destination.Setting.Section>(
//        metadata = SettingsDialogSceneStrategy.detailPane(),
    ) {
        SettingsScreen(
            titleKey = it.key,
            navigator = navigator,
        )
    }
}
