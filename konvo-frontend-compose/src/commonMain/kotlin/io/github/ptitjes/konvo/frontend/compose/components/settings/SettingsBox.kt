package io.github.ptitjes.konvo.frontend.compose.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*

enum class SettingsBoxOrientation {
    Vertical, Horizontal
}

@Composable
fun SettingsBox(
    title: String,
    description: String,
    orientation: SettingsBoxOrientation = SettingsBoxOrientation.Vertical,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    when (orientation) {
        SettingsBoxOrientation.Horizontal -> {
            Row(
                modifier = modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = title)
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                content()
            }
        }

        SettingsBoxOrientation.Vertical -> Column(
            modifier = modifier.fillMaxWidth().padding(bottom = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Text(text = title)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            content()
        }
    }
}
