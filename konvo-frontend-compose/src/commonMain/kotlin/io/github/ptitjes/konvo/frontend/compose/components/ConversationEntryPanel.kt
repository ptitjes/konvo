package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.mikepenz.markdown.m3.*

@Composable
fun ConversationEntryPanel(
    entry: ConversationEntry,
) {
    val horizontalArrangement = when (entry) {
        is ConversationEntry.Assistant -> Arrangement.Start
        is ConversationEntry.UserMessage -> Arrangement.End
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
                    is ConversationEntry.UserMessage -> MaterialTheme.colorScheme.primaryContainer
                }
            ) {
                Column {
                    val content = when (entry) {
                        is ConversationEntry.Assistant, is ConversationEntry.UserMessage -> entry.content
                    }

                    Markdown(
                        content = content,
                        modifier = Modifier.padding(12.dp),
                        colors = markdownColor(
                            text = when (entry) {
                                is ConversationEntry.Assistant -> MaterialTheme.colorScheme.onSurfaceVariant
                                is ConversationEntry.UserMessage -> MaterialTheme.colorScheme.onPrimaryContainer
                            },
                        ),
                    )

                    when (entry) {
                        is ConversationEntry.UserMessage -> {
                            entry.attachments.forEach { attachment ->
                                AttachmentView(attachment)
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}
