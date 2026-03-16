package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import androidx.compose.runtime.*
import coil3.compose.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.events.Messaging.*

@Composable
fun AttachmentView(
    attachment: Attachment,
) {
    AsyncImage(
        model = attachment.url,
        contentDescription = attachment.name,
    )
}
