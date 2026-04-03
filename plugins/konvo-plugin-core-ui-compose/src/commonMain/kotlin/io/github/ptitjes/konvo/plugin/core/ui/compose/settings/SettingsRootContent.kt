package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.navigation3.ui.*
import com.slack.circuit.foundation.*
import com.slack.circuit.runtime.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*

@Composable
internal fun SettingsRootContent(navigator: Navigator) {
    val windowSizeClass = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass

    val sceneStrategy = remember(windowSizeClass) {
        ListDetailStrategy<SettingsScreen>(windowSizeClass = windowSizeClass)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        SharedTransitionLayout {
            val sharedSettingsListContentState = rememberSharedContentState(SettingsListContentKey)

            val settingsListContent = remember {
                movableContentOf {
                    CircuitContent(
                        screen = SettingsListScreen,
                        navigator = navigator,
                        modifier = Modifier.sharedElement(
                            sharedContentState = sharedSettingsListContentState,
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                        ),
                    )
                }
            }

            val entryProvider = remember {
                circuitEntryProvider(navigator) {
                    entry<SettingsScreen>(
                        metadata = ListDetailScene.detail(defaultListContent = settingsListContent),
                    )
                }
            }

            val backStack = navigator.peekBackStack<SettingsScreen>()

            CircuitNavDisplay(
                navigator = navigator,
                backStack = backStack,
                sceneStrategies = listOf(sceneStrategy),
                sharedTransitionScope = this,
                transitionSpec = defaultKonvoTransitionSpec(),
                popTransitionSpec = defaultKonvoPopTransitionSpec(),
                entryProvider = entryProvider,
            )
        }
    }
}

private object SettingsListContentKey
