package io.github.ptitjes.konvo.plugin.core.ui.compose

import androidx.compose.runtime.*
import org.kodein.di.*

expect val appModule: DI.Module

interface App {
    @Composable
    operator fun invoke(onCloseRequest: () -> Unit)
}
