package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.runtime.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.images.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.theme.*
import io.github.ptitjes.konvo.frontend.compose.translations.*

@Composable
fun App() {
    CoilImageLoader()

    ProvideStrings(rememberStrings()) {
        KonvoTheme {
            MainScreen()
        }
    }
}
