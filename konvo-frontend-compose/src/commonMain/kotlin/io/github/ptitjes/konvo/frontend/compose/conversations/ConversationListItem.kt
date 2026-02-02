package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.frontend.compose.resources.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.text.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import org.jetbrains.compose.resources.*

@Composable
fun ConversationListItem(
    conversation: ConversationDigest,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val timestampFormatter = rememberRelativeTimestampFormatter()
    var showConfirm by remember { mutableStateOf(false) }

    val openConversationAria = strings.conversations.openConversationAria

    Surface(
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        val hasLastMessagePreview = !conversation.lastMessagePreview.isNullOrBlank()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    onClick(label = openConversationAria, action = null)
                }
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BadgedBox(
                    badge = {
                        val unreadMessageCount = conversation.unreadMessageCount
                        if (unreadMessageCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            ) {
                                Text(unreadMessageCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_chat),
                        contentDescription = strings.conversations.conversationAria,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = conversation.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = timestampFormatter.format(conversation.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = { showConfirm = true }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_delete),
                        contentDescription = strings.conversations.deleteConversationAria
                    )
                }
            }
            if (hasLastMessagePreview) {
                Text(
                    text = conversation.lastMessagePreview!!,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onDelete() }) {
                    Text(strings.conversations.deleteConfirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(strings.conversations.cancel)
                }
            },
            title = { Text(strings.conversations.deleteDialogTitle) },
            text = { Text(strings.conversations.deleteDialogText(conversation.title)) },
        )
    }
}
