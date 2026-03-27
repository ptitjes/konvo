package io.github.ptitjes.konvo.plugin.core.ui.compose.appearance

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import com.slack.circuit.runtime.screen.Screen
import io.github.ptitjes.konvo.plugin.core.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*

internal class AppearanceSettingsPresenter(
    private val settingsRepository: SettingsRepository,
) : Presenter<AppearanceSettingsView.State> {
    @Composable
    override fun present(): AppearanceSettingsView.State {
        var settings by settingsRepository.mutableSettingsOf(AppearanceSettingsKey)

        return AppearanceSettingsView.State(settings.baseColorScheme) { event ->
            when (event) {
                is AppearanceSettingsView.Event.UpdateScheme -> {
                    settings = settings.copy(baseColorScheme = event.scheme)
                }
            }
        }
    }
}

@Composable
fun SettingsPanelScope.AppearanceSettingsPanel(state: AppearanceSettingsView.State) {
    SettingsBox(
        title = i18n.appearance.baseColorSchemeTitle,
        description = i18n.appearance.baseColorSchemeDescription,
        bottomContent = {
            val optDark = i18n.appearance.baseColorSchemeOptionDark
            val optLight = i18n.appearance.baseColorSchemeOptionLight
            val optSystem = i18n.appearance.baseColorSchemeOptionSystem

            GenericSelector(
                modifier = Modifier.fillMaxWidth(),
                selectedItem = state.scheme,
                onSelectItem = { state.eventSink(AppearanceSettingsView.Event.UpdateScheme(it)) },
                options = BaseColorScheme.entries,
                itemLabeler = {
                    when (it) {
                        BaseColorScheme.Dark -> optDark
                        BaseColorScheme.Light -> optLight
                        BaseColorScheme.System -> optSystem
                    }
                },
            )
        }
    )
}

data object AppearanceSettingsView : Screen {
    data class State(
        val scheme: BaseColorScheme,
        val eventSink: (Event) -> Unit,
    ) : SettingsSectionState

    sealed interface Event : CircuitUiEvent {
        data class UpdateScheme(val scheme: BaseColorScheme) : Event
    }
}
