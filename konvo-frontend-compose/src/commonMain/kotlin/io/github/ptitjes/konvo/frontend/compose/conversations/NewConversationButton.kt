package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.material3.*
import androidx.compose.runtime.*
import io.github.ptitjes.konvo.frontend.compose.translations.*

@Composable
fun NewConversationButton(onNewClick: () -> Unit) {
    OutlinedButton(onClick = onNewClick) { Text(strings.conversations.startNewButton) }
}
