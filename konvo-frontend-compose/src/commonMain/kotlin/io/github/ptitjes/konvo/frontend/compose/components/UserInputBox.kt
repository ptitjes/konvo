package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.conversation.*

@Composable
fun UserInputBox(
    onSendMessage: (content: String, attachments: List<Attachment>) -> Unit,
) {
    var textInput by remember { mutableStateOf("") }
    val attachments = remember { mutableStateListOf<Attachment>() }

    val canSendMessage: Boolean = textInput.isNotBlank()

    fun sendMessage() {
        val content = textInput.trim()
        if (content.isBlank()) return

        onSendMessage(content, attachments.toList())

        textInput = ""
        attachments.clear()
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp,
    ) {
        Column {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AddAttachmentsButton(
                    onAddAttachments = { attachments.addAll(it) },
                )

                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .onPreviewKeyEvent {
                            if (it.key == Key.Enter && !it.isShiftPressed) {
                                if (canSendMessage) sendMessage()
                                true
                            } else false
                        },
                    placeholder = { Text("Type a message") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
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

            if (attachments.isNotEmpty()) {
                SelectedAttachementsView(attachments)
            }
        }
    }
}
