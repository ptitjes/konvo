package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import androidx.compose.material3.*
import androidx.compose.runtime.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*

@Composable
fun NewConversationButton(onNewClick: () -> Unit) {
    OutlinedButton(onClick = onNewClick) { Text(i18n.conversations.startNewButton) }
}
