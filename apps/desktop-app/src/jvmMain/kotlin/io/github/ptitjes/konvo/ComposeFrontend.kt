package io.github.ptitjes.konvo

import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import org.kodein.di.*

fun DI.startComposeFrontend() = application {
    val app by di.instance<App>()

    Window(
        title = "Konvo",
        state = rememberWindowState(width = 375.dp, height = 667.dp),
//                state = rememberWindowState(width = 1280.dp, height = 720.dp),
        icon = appIconPainter(),
        onCloseRequest = ::exitApplication,
        onKeyEvent = { event ->
            if (event.type == KeyEventType.Companion.KeyUp && event.isCtrlPressed && event.key == Key.Companion.Q) {
                exitApplication()
                true
            } else {
                false
            }
        },
    ) {
        val containerSize = LocalWindowInfo.current.containerSize
        if (containerSize != IntSize(0, 0)) {
            app()
        }
    }
}
