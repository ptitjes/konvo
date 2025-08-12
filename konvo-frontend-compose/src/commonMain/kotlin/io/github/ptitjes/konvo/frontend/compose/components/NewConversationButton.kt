package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun NewConversationButton(onNewClick: () -> Unit) {
    OutlinedButton(onClick = onNewClick) { Text("Start new conversation") }
}
