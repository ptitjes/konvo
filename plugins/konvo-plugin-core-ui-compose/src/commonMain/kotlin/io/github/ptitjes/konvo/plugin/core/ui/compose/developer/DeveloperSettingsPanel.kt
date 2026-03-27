package io.github.ptitjes.konvo.plugin.core.ui.compose.developer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import io.github.ptitjes.konvo.plugin.core.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*

internal data object DeveloperSettingsView {
    data class State(
        val settings: DeveloperSettings,
        val eventSink: (Event) -> Unit,
    ) : SettingsSectionState

    sealed interface Event : CircuitUiEvent {
        data class UpdateSettings(val settings: DeveloperSettings) : Event
    }
}

internal class DeveloperSettingsPresenter(
    private val settingsRepository: SettingsRepository,
) : Presenter<DeveloperSettingsView.State> {
    @Composable
    override fun present(): DeveloperSettingsView.State {
        var settings by settingsRepository.mutableSettingsOf(DeveloperSettingsKey)

        return DeveloperSettingsView.State(settings) { event ->
            when (event) {
                is DeveloperSettingsView.Event.UpdateSettings -> {
                    settings = event.settings
                }
            }
        }
    }
}

@Composable
internal fun DeveloperSettingsPanel(state: DeveloperSettingsView.State) {
    val settings = state.settings

    SettingsBox(
        title = i18n.developer.openTelemetryTitle,
        description = i18n.developer.openTelemetryDescription,
        bottomContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = i18n.developer.openTelemetryEnabledLabel,
                    )
                    Switch(
                        checked = settings.openTelemetry.enabled,
                        onCheckedChange = { enabled ->
                            state.eventSink(
                                DeveloperSettingsView.Event.UpdateSettings(
                                    settings.copy(
                                        openTelemetry = settings.openTelemetry.copy(enabled = enabled)
                                    )
                                )
                            )
                        }
                    )
                }

                if (settings.openTelemetry.enabled) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = i18n.developer.openTelemetryVerboseLabel,
                        )
                        Switch(
                            checked = settings.openTelemetry.verbose,
                            onCheckedChange = { verbose ->
                                state.eventSink(
                                    DeveloperSettingsView.Event.UpdateSettings(
                                        settings.copy(
                                            openTelemetry = settings.openTelemetry.copy(verbose = verbose)
                                        )
                                    )
                                )
                            }
                        )
                    }

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = settings.openTelemetry.endpoint,
                        onValueChange = { endpoint ->
                            state.eventSink(
                                DeveloperSettingsView.Event.UpdateSettings(
                                    settings.copy(
                                        openTelemetry = settings.openTelemetry.copy(endpoint = endpoint)
                                    )
                                )
                            )
                        },
                        label = { Text(i18n.developer.openTelemetryEndpointLabel) },
                        singleLine = true,
                    )
                }
            }
        }
    )
}
