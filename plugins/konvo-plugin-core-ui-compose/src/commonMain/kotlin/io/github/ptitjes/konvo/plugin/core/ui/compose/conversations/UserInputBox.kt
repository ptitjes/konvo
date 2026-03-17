package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.events.Messaging.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import org.jetbrains.compose.resources.*

@Composable
fun UserInputBox(
    modifier: Modifier = Modifier,
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
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
                    placeholder = {
                        Text(
                            text = i18n.conversations.inputPlaceholder,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        focusedSupportingTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedSupportingTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledSupportingTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        cursorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    maxLines = 5,
                )

                IconButton(
                    onClick = { if (canSendMessage) sendMessage() },
                    enabled = canSendMessage,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_send),
                        contentDescription = i18n.conversations.sendMessageAria,
                    )
                }
            }

            if (attachments.isNotEmpty()) {
                SelectedAttachementsView(attachments)
            }
        }
    }
}
