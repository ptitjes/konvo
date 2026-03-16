package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import androidx.compose.material3.*
import androidx.compose.runtime.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*

@Composable
fun NewConversationButton(onNewClick: () -> Unit) {
    OutlinedButton(onClick = onNewClick) { Text(strings.conversations.startNewButton) }
}
