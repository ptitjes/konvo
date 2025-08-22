package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.frontend.compose.translations.*

@Composable
fun UserInputBox(
    onSendMessage: (content: String, attachments: List<Attachment>) -> Unit,
) {
    var inputValue by remember { mutableStateOf(TextFieldValue(text = "")) }
    val attachments = remember { mutableStateListOf<Attachment>() }

    val canSendMessage: Boolean = inputValue.text.isNotBlank()

    fun sendMessage() {
        val content = inputValue.text.trim()
        if (content.isBlank()) return

        onSendMessage(content, attachments.toList())

        inputValue = inputValue.copy(text = "")
        attachments.clear()
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp,
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AddAttachmentsButton(
                    onAddAttachments = { attachments.addAll(it) },
                )

                TextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    modifier = Modifier
                        .weight(1f)
                        .onPreviewKeyEvent {
                            // Allow Shift+Enter to insert a new line; Enter alone sends the message
                            if (it.type == KeyEventType.KeyDown) {
                                val isEnter = it.key == Key.Enter || it.key == Key.NumPadEnter
                                if (isEnter) {
                                    if (!it.isShiftPressed) {
                                        if (canSendMessage) sendMessage()
                                    } else {
                                        inputValue = inputValue.copy(
                                            text = "${inputValue.text}\n",
                                            selection = TextRange(inputValue.text.length + 1),
                                        )
                                    }
                                    return@onPreviewKeyEvent true
                                }
                            }
                            false
                        },
                    placeholder = { Text(strings.conversations.inputPlaceholder) },
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
                        contentDescription = strings.conversations.sendMessageAria,
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
