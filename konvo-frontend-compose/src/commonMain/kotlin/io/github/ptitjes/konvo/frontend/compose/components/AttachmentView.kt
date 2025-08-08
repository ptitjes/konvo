package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.runtime.Composable
import coil3.compose.AsyncImage
import io.github.ptitjes.konvo.core.conversation.Attachment
import io.github.vinceglb.filekit.core.PlatformFile

@Composable
fun AttachmentView(
    attachment: Attachment,
) {
    AsyncImage(
        model = attachment.url,
        contentDescription = attachment.name,
    )
}
