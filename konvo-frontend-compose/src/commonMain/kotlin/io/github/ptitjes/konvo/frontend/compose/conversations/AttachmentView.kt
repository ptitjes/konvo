package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.runtime.*
import coil3.compose.*
import io.github.ptitjes.konvo.core.conversations.model.*

@Composable
fun AttachmentView(
    attachment: Attachment,
) {
    AsyncImage(
        model = attachment.url,
        contentDescription = attachment.name,
    )
}
