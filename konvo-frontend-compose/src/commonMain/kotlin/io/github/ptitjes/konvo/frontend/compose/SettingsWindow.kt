package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.navigation3.runtime.*
import io.github.ptitjes.konvo.frontend.compose.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.settings.*

@Composable
fun SettingsWindow(
    navigator: SettingsNavigator,
    modifier: Modifier = Modifier,
) {
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
            CompositionLocalProvider(
                LocalListDetailPaneType provides ListDetailPaneType.TwoPane,
            ) {
                SettingsScreenScaffold(
                    modifier = modifier,
                    backStack = navigator.backStack,
                    onBack = {
                        if (navigator.isLastSettingsSection) {
                            navigator.closeSettings()
                        } else {
                            navigator.navigateBack()
                        }
                    },
                    entryProvider = entryProvider {

                        entry<SettingsDestination.List>(metadata = ListDetailScene.list()) {
                            SettingsListScreen(navigator = navigator)
                        }

                        entry<SettingsDestination.Section>(metadata = ListDetailScene.detail()) {
                            SettingsScreen(titleKey = it.key, navigator = navigator)
                        }
                    }
                )
            }
        }
    }
}
