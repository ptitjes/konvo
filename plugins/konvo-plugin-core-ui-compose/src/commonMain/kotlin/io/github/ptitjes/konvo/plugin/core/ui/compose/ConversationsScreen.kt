package io.github.ptitjes.konvo.plugin.core.ui.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.lifecycle.viewmodel.navigation3.*
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.viewmodels.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*

@Composable
internal fun ConversationsScreen(
    viewModel: MainScreenViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val navigationPaneState = rememberPaneState(PaneValue.Collapsed)
    val extraPaneState = rememberPaneState(PaneValue.Collapsed)

    val conversationNavigator = viewModel.conversationNavigator

    val settingsBackStack by viewModel.settingsBackStack.collectAsState(initial = emptyList())

    SettingsWindow(
        navigator = viewModel.settingsNavigator,
        backStack = settingsBackStack
    )

    val backStack by viewModel.conversationBackStack.collectAsState(null)

    LaunchedEffect(backStack) {
        println("Back stack changed: ${backStack?.joinToString(", ")}")
    }

    when (val backStack = backStack) {
        null -> FullSizeProgressIndicator(modifier = modifier)
        else -> MainScreenScaffold(
            modifier = modifier,
            backStack = backStack,
            onBack = { conversationNavigator.goBack() },
            navigationPaneState = navigationPaneState,
            extraPaneState = extraPaneState,
            entryProvider = entryProvider {

                entry<Destination.Conversation.List>(metadata = CenterStageScene.navigation()) {
                    ConversationListScreen(navigator = conversationNavigator)
                }

                entry<Destination.Conversation.New>(metadata = CenterStageScene.content()) {
                    AgentConfigurationScreen(navigator = conversationNavigator)
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
                    ConversationScreen(conversationId = it.id, navigator = conversationNavigator)
                }
            }
        )
    }
}

@Composable
private fun <T : Any> MainScreenScaffold(
    backStack: List<T>,
    onBack: () -> Unit,
    navigationPaneState: PaneState,
    extraPaneState: PaneState,
    entryProvider: (key: T) -> NavEntry<T>,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass

    val sceneStrategy = remember(windowSizeClass, navigationPaneState, extraPaneState) {
        CenterStageSceneStrategy<T>(
            windowSizeClass = windowSizeClass,
            navigationPaneState = navigationPaneState,
            extraPaneState = extraPaneState,
        )
    }

    Surface(modifier = modifier) {
        SharedTransitionLayout {
            NavDisplay(
                backStack = backStack,
                onBack = onBack,
                sceneStrategy = sceneStrategy,
                sharedTransitionScope = this,
                entryDecorators = listOf<NavEntryDecorator<T>>(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider,
                transitionSpec = {
                    fadeIn(animationSpec = tween(durationMillis = 250)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 250))
                }
            )
        }
    }
}
