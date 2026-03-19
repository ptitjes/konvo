package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import org.jetbrains.compose.resources.*

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnavailabilityPlaceholder(
    unavailabilityText: String,
    onGoToSettings: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = unavailabilityText,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp).weight(1f),
        )

        if (onGoToSettings != null) {
            TextButton(
                modifier = Modifier.height(32.dp),
                onClick = { onGoToSettings() },
                contentPadding =
                    PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 6.dp,
                        bottom = 6.dp,
                    ),
                shape = MaterialTheme.shapes.small,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val contentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(Res.drawable.ic_settings),
                        contentDescription = i18n.settings.listTitle,
                        tint = contentColor,
                    )

                    Text(
                        text = i18n.settings.listTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                    )
                }
            }
        }
    }
}
