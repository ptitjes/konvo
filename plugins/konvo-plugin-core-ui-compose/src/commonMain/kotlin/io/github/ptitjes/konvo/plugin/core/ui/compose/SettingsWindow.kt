package io.github.ptitjes.konvo.plugin.core.ui.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.lifecycle.viewmodel.navigation3.*
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.*
import com.slack.circuit.foundation.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*

@Composable
internal fun SettingsWindow(
    navigator: SettingsNavigator,
    backStack: List<SettingsScreen>,
) {
    if (backStack.isNotEmpty()) {
        // TODO provide the dialog window as an expect/actual
        // TODO derive the dialog window size based on screen size
        DialogWindow(
            title = "Settings",
            state = rememberDialogState(
                size = DpSize(width = 1024.dp, height = 800.dp),
            ),
            onCloseRequest = { navigator.closeSettings() },
        ) {
            val containerSize = LocalWindowInfo.current.containerSize
            if (containerSize != IntSize(0, 0)) {
                SettingsWindowContent(navigator, backStack)
            }
        }
    }
}

@Composable
private fun SettingsWindowContent(
    navigator: SettingsNavigator,
    backStack: List<SettingsScreen>,
) {
    CompositionLocalProvider(
        LocalListDetailPaneType provides ListDetailPaneType.TwoPane,
    ) {
        SettingsScreenScaffold(
            modifier = Modifier.fillMaxSize(),
            backStack = backStack,
            onBack = {
                if (navigator.isLastSection) {
                    navigator.closeSettings()
                } else {
                    navigator.goBack()
                }
            },
            entryProvider = entryProvider {

                entry<SettingsListScreen>(metadata = ListDetailScene.list()) {
                    CircuitContent(screen = it, navigator = navigator.navigator)
                }

                entry<SettingsSectionScreen>(metadata = ListDetailScene.detail()) {
                    CircuitContent(screen = it, navigator = navigator.navigator)
                }
            }
        )
    }
}

@Composable
private fun <T : Any> SettingsScreenScaffold(
    backStack: List<T>,
    onBack: () -> Unit,
    entryProvider: (key: T) -> NavEntry<T>,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass

    val sceneStrategy = remember(windowSizeClass) {
        ListDetailStrategy<T>(windowSizeClass = windowSizeClass)
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
                },
                popTransitionSpec = {
                    fadeIn(animationSpec = tween(durationMillis = 250)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 250))
                }
            )
        }
    }
}
