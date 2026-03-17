package io.github.ptitjes.konvo.plugin.core.ui.compose.developer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.plugin.core.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*

@Composable
fun DeveloperSettingsPanel() {
    var settings by rememberMutableSettings(DeveloperSettingsKey)

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
                            text = i18n.developer.openTelemetryVerboseLabel,
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
                        label = { Text(i18n.developer.openTelemetryEndpointLabel) },
                        singleLine = true,
                    )
                }
            }
        }
    )
}
