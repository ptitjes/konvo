package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*

@Composable
fun ConversationEntryPanel(
    entry: ConversationEntry,
) {
    val horizontalArrangement = when (entry) {
        is ConversationEntry.Assistant -> Arrangement.Start
        is ConversationEntry.User -> Arrangement.End
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(.80f),
            horizontalArrangement = horizontalArrangement,
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when (entry) {
                    is ConversationEntry.Assistant -> MaterialTheme.colorScheme.surfaceVariant
                    is ConversationEntry.User -> MaterialTheme.colorScheme.primaryContainer
                }
            ) {
                Text(
                    text = entry.content,
                    modifier = Modifier.padding(12.dp),
                    color = when (entry) {
                        is ConversationEntry.Assistant -> MaterialTheme.colorScheme.onSurfaceVariant
                        is ConversationEntry.User -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }
    }
}
