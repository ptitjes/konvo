package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.*

@Composable
fun UserInputBox(viewModel: ConversationViewModel) {
    var textInput by remember { mutableStateOf("") }

    val canSendMessage: Boolean = textInput.isNotBlank()

    fun sendMessage() {
        val message = textInput.trim()
        textInput = ""

        viewModel.sendUserMessage(message)
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { /* TODO Handle attachment */ }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add an attachment",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .weight(1f)
                    .onPreviewKeyEvent {
                        if (it.key == Key.Enter && !it.isShiftPressed && canSendMessage) {
                            sendMessage()
                            true
                        } else false
                    },
                placeholder = { Text("Type a message") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                maxLines = 5,
            )

            IconButton(
                onClick = { if (canSendMessage) sendMessage() },
                enabled = canSendMessage,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send the message",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
