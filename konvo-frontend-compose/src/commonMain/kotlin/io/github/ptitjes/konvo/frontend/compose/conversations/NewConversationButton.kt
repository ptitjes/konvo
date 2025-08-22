package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun NewConversationButton(onNewClick: () -> Unit) {
    OutlinedButton(onClick = onNewClick) { Text("Start new conversation") }
}
