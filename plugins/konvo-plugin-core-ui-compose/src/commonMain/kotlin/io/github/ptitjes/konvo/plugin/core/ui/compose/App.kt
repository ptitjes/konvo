package io.github.ptitjes.konvo.plugin.core.ui.compose

import androidx.compose.runtime.*
import com.slack.circuit.foundation.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.images.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.theme.*
import org.kodein.di.*
import org.kodein.di.compose.*

class App(private val di: DI) {
    @Composable
    operator fun invoke() {
        val circuit by di.instance<Circuit>()

        // TODO do not expose this to the UI
        withDI(di) {
            CoilImageLoader()

            ProvideI18nStrings {
                KonvoTheme {
                    CircuitCompositionLocals(circuit = circuit) {
                        ConversationsScreen()
                    }
                }
            }
        }
    }
}
