package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.runtime.Composable
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.core.PlatformFile

@Composable
fun AttachmentView(
    attachment: PlatformFile,
) {
    AsyncImage(
        model = attachment.file.toURI().toString(),
        contentDescription = attachment.name,
    )
}
