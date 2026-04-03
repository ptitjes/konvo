package io.github.ptitjes.konvo

import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import org.jetbrains.compose.resources.*
import org.kodein.di.*

fun DI.startComposeFrontend() = application {
    val app by di.instance<App>()

    // TODO store window state
    val state = rememberWindowState(width = 375.dp, height = 667.dp)
//    val state = rememberWindowState(width = 1280.dp, height = 720.dp)

    val keyEventHandler: (KeyEvent) -> Boolean = { event ->
        when {
            event.type == KeyEventType.KeyUp && event.isCtrlPressed && event.key == Key.Q -> {
                exitApplication()
                true
            }

            else -> false
        }
    }

    Window(
        title = "Konvo",
        state = state,
        icon = painterResource(CoreResources.appIcon),
        onCloseRequest = ::exitApplication,
        onKeyEvent = keyEventHandler,
    ) {
        // Work around https://youtrack.jetbrains.com/issue/CMP-8821
        val containerSize = LocalWindowInfo.current.containerSize
        if (containerSize != IntSize(0, 0)) {
            app()
        }
    }
}
