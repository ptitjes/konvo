package io.github.ptitjes.konvo.frontend.compose.conversations

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.vinceglb.filekit.core.*

actual fun PlatformFile.createImageAttachement(): Attachment {
    return Attachment(
        type = Attachment.Type.Image,
        url = file.toURI().toString(),
        name = name,
        mimeType = "image/$extension",
    )
}
