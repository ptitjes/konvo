package io.github.ptitjes.konvo.plugin.core.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.navigation3.runtime.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*

@Composable
fun SettingsWindow(navigator: SettingsNavigator) {
    if (navigator.backStack.isNotEmpty()) {
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
                SettingsWindowContent(navigator)
            }
        }
    }
}

@Composable
private fun SettingsWindowContent(
    navigator: SettingsNavigator,
) {
    CompositionLocalProvider(
        LocalListDetailPaneType provides ListDetailPaneType.TwoPane,
    ) {
        SettingsScreenScaffold(
            modifier = Modifier.fillMaxSize(),
            backStack = navigator.backStack,
            onBack = {
                if (navigator.isLastSettingsSection) {
                    navigator.closeSettings()
                } else {
                    navigator.navigateBack()
                }
            },
            entryProvider = entryProvider {

                entry<SettingsDestination.List>(metadata = ListDetailScene.Companion.list()) {
                    SettingsListScreen(navigator = navigator)
                }

                entry<SettingsDestination.Section>(metadata = ListDetailScene.Companion.detail()) {
                    SettingsScreen(titleKey = it.key, navigator = navigator)
                }
            }
        )
    }
}
