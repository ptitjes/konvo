package io.github.ptitjes.konvo.frontend.compose.toolkit.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*

@Composable
fun SettingsBox(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit = {},
    bottomContent: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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

            trailingContent()
        }

        bottomContent()
    }
}
