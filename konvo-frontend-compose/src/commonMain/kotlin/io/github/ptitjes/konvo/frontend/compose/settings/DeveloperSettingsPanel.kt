package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.settings.*
import io.github.ptitjes.konvo.frontend.compose.translations.*

@Composable
fun DeveloperSettingsPanel() {
    var settings by rememberMutableSettings(DeveloperSettingsKey)

    SettingsBox(
        title = strings.settings.developerOpenTelemetryTitle,
        description = strings.settings.developerOpenTelemetryDescription,
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
                        text = strings.settings.developerOpenTelemetryEnabledLabel,
                    )
                    Switch(
                        checked = settings.openTelemetry.enabled,
                        onCheckedChange = { enabled ->
                            settings = settings.copy(
                                openTelemetry = settings.openTelemetry.copy(enabled = enabled)
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
                            text = strings.settings.developerOpenTelemetryVerboseLabel,
                        )
                        Switch(
                            checked = settings.openTelemetry.verbose,
                            onCheckedChange = { verbose ->
                                settings = settings.copy(
                                    openTelemetry = settings.openTelemetry.copy(verbose = verbose)
                                )
                            }
                        )
                    }

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = settings.openTelemetry.endpoint,
                        onValueChange = { endpoint ->
                            settings = settings.copy(
                                openTelemetry = settings.openTelemetry.copy(endpoint = endpoint)
                            )
                        },
                        label = { Text(strings.settings.developerOpenTelemetryEndpointLabel) },
                        singleLine = true,
                    )
                }
            }
        }
    )
}
