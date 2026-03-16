package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import io.github.ptitjes.konvo.plugin.core.conversations.model.events.Messaging.*
import io.github.vinceglb.filekit.*

actual fun PlatformFile.createImageAttachment(): Attachment {
    return Attachment(
        type = Attachment.Type.Image,
        url = file.toURI().toString(),
        name = file.name,
        mimeType = "image/${file.extension}",
    )
}
