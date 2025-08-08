package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.mikepenz.markdown.m3.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.ConversationUiEntry

@Composable
fun ConversationEntryPanel(
    entry: ConversationUiEntry,
) {
    val horizontalArrangement = when (entry) {
        is ConversationUiEntry.Assistant -> Arrangement.Start
        is ConversationUiEntry.UserMessage -> Arrangement.End
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
                    is ConversationUiEntry.Assistant -> MaterialTheme.colorScheme.surfaceVariant
                    is ConversationUiEntry.UserMessage -> MaterialTheme.colorScheme.primaryContainer
                }
            ) {
                Column {
                    val content = when (entry) {
                        is ConversationUiEntry.Assistant, is ConversationUiEntry.UserMessage -> entry.content
                    }

                    Markdown(
                        content = content,
                        modifier = Modifier.padding(12.dp),
                        colors = markdownColor(
                            text = when (entry) {
                                is ConversationUiEntry.Assistant -> MaterialTheme.colorScheme.onSurfaceVariant
                                is ConversationUiEntry.UserMessage -> MaterialTheme.colorScheme.onPrimaryContainer
                            },
                        ),
                    )

                    when (entry) {
                        is ConversationUiEntry.UserMessage -> {
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
