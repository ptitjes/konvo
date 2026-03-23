package io.github.ptitjes.konvo

import androidx.compose.ui.window.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import org.kodein.di.*

fun DI.startComposeFrontend() = application {
    val app by di.instance<App>()
    app(::exitApplication)
}
