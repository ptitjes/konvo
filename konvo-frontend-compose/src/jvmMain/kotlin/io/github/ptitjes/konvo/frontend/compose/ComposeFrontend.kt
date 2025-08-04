package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.ui.window.*
import io.github.ptitjes.konvo.core.*
import kotlinx.coroutines.*

suspend fun Konvo.composeFrontend() = coroutineScope {
    application {
        Window(
            title = "Konvo",
            onCloseRequest = ::exitApplication,
        ) {
            App()
        }
    }
}
