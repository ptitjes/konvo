package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.desktop.ui.tooling.preview.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.theme.*
import io.github.ptitjes.konvo.frontend.compose.translations.*

@Composable
fun NewMessagesDivider() {
    Box(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        HorizontalDivider(
            modifier = Modifier.align(Alignment.CenterEnd),
            color = MaterialTheme.colorScheme.error,
        )
        Text(
            text = strings.conversations.newMessagesLabel,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 4.dp)
        )
    }
}

@Preview
@Composable
private fun NewMessagesDividerPreview() {
    KonvoTheme {
        NewMessagesDivider()
    }
}
