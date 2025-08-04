package io.github.ptitjes.konvo.frontend.compose.attachments

import ai.koog.prompt.message.*
import io.github.vinceglb.filekit.core.*

actual fun PlatformFile.createFileAttachement(): Attachment {
    val fileName = file.name
    val format = fileName.substringAfterLast('.')

    val bytes = file.readBytes()

    return Attachment.Image(
        content = AttachmentContent.Binary.Bytes(bytes),
        fileName = fileName,
        format = format,
    )
}
