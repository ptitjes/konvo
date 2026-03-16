package io.github.ptitjes.konvo.plugin.core.ui.compose

import androidx.compose.runtime.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.images.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.theme.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import org.kodein.di.*
import org.kodein.di.compose.*

class App(private val di: DI) {
    @Composable
    operator fun invoke() {
        withDI(di) {
            CoilImageLoader()

            ProvideStrings(rememberStrings()) {
                KonvoTheme {
                    MainScreen()
                }
            }
        }
    }
}
