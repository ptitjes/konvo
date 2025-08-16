package io.github.ptitjes.konvo.frontend.compose.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*

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
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
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
            modifier = modifier.fillMaxWidth(),
        ) {
            Text(text = title)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
            )

            content()
        }
    }
}
