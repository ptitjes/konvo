package io.github.ptitjes.konvo.plugin.core.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.slack.circuit.foundation.*
import com.slack.circuit.foundation.navstack.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import org.kodein.di.*

class App(private val di: DI) {
    @Composable
    operator fun invoke() {
        KonvoCompositionLocals(di) {
            val navStack = rememberSaveableNavStack(NewConversationScreen)

            val navigator = rememberCircuitNavigator(navStack) {
                // TODO maybe close app?
            }

            ConversationRootScreen(
                modifier = Modifier.fillMaxSize(),
                navigator = navigator,
            )

            SettingsWindow(navigator = navigator)
        }
    }
}
