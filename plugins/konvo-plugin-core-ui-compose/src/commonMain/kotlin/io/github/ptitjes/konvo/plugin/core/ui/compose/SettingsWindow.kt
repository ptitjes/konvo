package io.github.ptitjes.konvo.plugin.core.ui.compose

import androidx.compose.runtime.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.slack.circuit.runtime.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*

@Composable
internal fun SettingsWindow(navigator: Navigator) {
    if (navigator.peekBackStack<SettingsScreen>().isNotEmpty()) {
        fun closeSettings() {
            navigator.popUntil { it !is SettingsScreen }
        }

        // TODO provide the dialog window as an expect/actual
        // TODO derive the dialog window size based on screen size

        val state = rememberDialogState(
            size = DpSize(width = 1024.dp, height = 800.dp),
        )

        val keyEventHandler: (KeyEvent) -> Boolean = { event ->
            val lastSettingsScreen = navigator.peekBackStack<SettingsScreen>().size == 1
            if (event.key == Key.Escape && event.type == KeyEventType.KeyDown && lastSettingsScreen) {
                closeSettings()
                true
            } else false
        }

        DialogWindow(
            title = "Settings",
            state = state,
            onCloseRequest = { closeSettings() },
            onKeyEvent = keyEventHandler,
        ) {
            // Work around https://youtrack.jetbrains.com/issue/CMP-8821
            val containerSize = LocalWindowInfo.current.containerSize
            if (containerSize != IntSize(0, 0)) {
                SettingsRootContent(navigator)
            }
        }
    }
}
