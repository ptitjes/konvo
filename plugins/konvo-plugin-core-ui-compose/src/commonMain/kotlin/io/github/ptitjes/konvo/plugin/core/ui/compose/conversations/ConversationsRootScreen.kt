package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.navigation3.ui.*
import com.slack.circuit.foundation.*
import com.slack.circuit.runtime.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*

@Composable
internal fun ConversationRootScreen(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    navigationPaneState: PaneState = rememberPaneState(PaneValue.Collapsed),
    extraPaneState: PaneState = rememberPaneState(PaneValue.Collapsed),
) {
    val windowSizeClass = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass

    val sceneStrategy = remember(windowSizeClass, navigationPaneState, extraPaneState) {
        CenterStageSceneStrategy<ConversationsScreen>(
            windowSizeClass = windowSizeClass,
            navigationPaneState = navigationPaneState,
            extraPaneState = extraPaneState,
        )
    }

    Surface(modifier = modifier) {
        SharedTransitionLayout {
            val sharedSettingsListContentState = rememberSharedContentState(ConversationNavigationContentKey)

            val navigationContent = remember {
                movableContentOf {
                    CircuitContent(
                        screen = ConversationListScreen,
                        navigator = navigator,
                        modifier = Modifier.sharedElement(
                            sharedContentState = sharedSettingsListContentState,
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                        ),
                    )
                }
            }

            val entryProvider = circuitEntryProvider(navigator) {
                entry<NewConversationScreen>(
                    metadata = CenterStageScene.content(
                        defaultNavigationContent = navigationContent,
                    ),
                ) { AgentConfigurationScreen }

                entry<ConversationScreen>(
                    metadata = CenterStageScene.content(
                        defaultNavigationContent = navigationContent,
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
                )
            }

            val backStack = navigator.peekBackStack<ConversationsScreen>()

            CircuitNavDisplay(
                navigator = navigator,
                backStack = backStack,
                sceneStrategy = sceneStrategy,
                sharedTransitionScope = this,
                transitionSpec = defaultKonvoTransitionSpec(),
                popTransitionSpec = defaultKonvoPopTransitionSpec(),
                entryProvider = entryProvider,
            )
        }
    }
}

private object ConversationNavigationContentKey
