package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*

@Composable
fun OutlineBox(
    label: String,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier.padding(top = 8.dp).fillMaxWidth().border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(4.dp)
            )
        ) {
            Box(
                modifier = Modifier.padding(16.dp, 8.dp),
            ) {
                content()
            }
        }

        Row(
            modifier = Modifier.offset(x = 12.dp, y = (-2).dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
