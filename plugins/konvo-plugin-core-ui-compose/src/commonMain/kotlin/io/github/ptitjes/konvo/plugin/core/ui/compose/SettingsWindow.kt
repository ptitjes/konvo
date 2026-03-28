package io.github.ptitjes.konvo.plugin.core.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.navigation3.runtime.*
import com.slack.circuit.foundation.*
import com.slack.circuit.runtime.ui.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import org.kodein.di.*
import org.kodein.di.compose.*

@Composable
fun SettingsWindow(navigator: SettingsNavigator) {
    if (navigator.backStack.isNotEmpty()) {
        // TODO provide the dialog window as an expect/actual
        // TODO derive the dialog window size based on screen size
        DialogWindow(
            title = "Settings",
            state = rememberDialogState(
                size = DpSize(width = 1024.dp, height = 800.dp),
            ),
            onCloseRequest = { navigator.closeSettings() },
        ) {
            val containerSize = LocalWindowInfo.current.containerSize
            if (containerSize != IntSize(0, 0)) {
                SettingsWindowContent(navigator)
            }
        }
    }
}

private fun <S : SettingsSectionState> uiForSection(section: SettingsSection<S>): Ui<S> {
    return ui { state, modifier -> section.panel(state) }
}

@Composable
private fun SettingsWindowContent(
    navigator: SettingsNavigator,
) {
    val sectionManager by rememberInstance<SettingsSectionManager>()
    val di = localDI()

    val circuit = remember {
        Circuit.Builder()
            .addPresenterFactory { screen, _, _ ->
                when (screen) {
                    is SettingsListScreen -> di.direct.newInstance {
                        new(::SettingsListPresenter, navigator)
                    }

                    is SettingsSectionScreen -> di.direct.newInstance {
                        new(::SettingsSectionPresenter, a1 = screen, a2 = navigator)
                    }

                    is SettingsSectionView -> sectionManager.sectionForKey(screen.key)
                        .presenterFactory(navigator)

                    else -> null
                }
            }
            .addUiFactory { screen, _ ->
                when (screen) {
                    is SettingsListScreen -> ui(::SettingsListScreen)
                    is SettingsSectionScreen -> ui(::SettingsSectionScreen)
                    is SettingsSectionView -> uiForSection(sectionManager.sectionForKey(screen.key))
                    else -> null
                }
            }
            .build()
    }

    CircuitCompositionLocals(
        circuit = circuit,
    ) {
        CompositionLocalProvider(
            LocalListDetailPaneType provides ListDetailPaneType.TwoPane,
        ) {
            SettingsScreenScaffold(
                modifier = Modifier.fillMaxSize(),
                backStack = navigator.backStack,
                onBack = {
                    if (navigator.isLastSettingsSection) {
                        navigator.closeSettings()
                    } else {
                        navigator.navigateBack()
                    }
                },
                entryProvider = entryProvider {

                    entry<SettingsListScreen>(metadata = ListDetailScene.list()) {
                        CircuitContent(screen = it)
                    }

                    entry<SettingsSectionScreen>(metadata = ListDetailScene.detail()) {
                        CircuitContent(screen = it)
                    }
                }
            )
        }
    }
}
