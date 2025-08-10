package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.runtime.*
import coil3.compose.*
import io.github.ptitjes.konvo.core.conversation.model.*

@Composable
fun AttachmentView(
    attachment: Attachment,
) {
    AsyncImage(
        model = attachment.url,
        contentDescription = attachment.name,
    )
}
