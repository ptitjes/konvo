package io.github.ptitjes.konvo.core.agents.toolkit

import ai.koog.prompt.message.*
import com.eygraber.uri.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.Message
import io.github.ptitjes.konvo.core.util.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.datetime.*
import kotlinx.io.files.*
import ai.koog.prompt.message.ContentPart as KoogContentPart
import ai.koog.prompt.message.Message as KoogMessage

internal suspend fun Action<Message>.toKoogMessage(): KoogMessage = when (sender) {
    is Participant.User -> KoogMessage.User(
        parts = payload.content.map { it.toKoogContentPart() },
        metaInfo = RequestMetaInfo(timestamp = timestamp.toDeprecatedInstant()),
    )

    is Participant.Agent -> KoogMessage.Assistant(
        parts = payload.content.map { it.toKoogContentPart() },
        metaInfo = ResponseMetaInfo(timestamp = timestamp.toDeprecatedInstant()),
    )
}

private suspend fun Messaging.Part.toKoogContentPart(): KoogContentPart = when (this) {
    is Messaging.Part.Text -> KoogContentPart.Text(text)
    is Messaging.Part.Image -> media.toKoogAttachment()
    is Messaging.Part.Video -> media.toKoogAttachment()
    is Messaging.Part.Audio -> media.toKoogAttachment()
    is Messaging.Part.File -> media.toKoogAttachment()
    is Messaging.Part.Embed<*> -> error("Embed content part is not supported for Koog")
}

private val httpClient = HttpClient(CIO)

private suspend fun Messaging.Attachment.loadContent(): ByteArray {
    val uri = Uri.parse(url)

    return when {
        uri.scheme == "file" -> {
            val path = Path(uri.path ?: error("Invalid file path: $url"))
            SystemFileSystem.readBytes(path).toByteArray()
        }

        else -> httpClient.get(url).bodyAsBytes()
    }
}

private suspend fun Messaging.Attachment.toKoogAttachment(): KoogContentPart {
    val bytes = loadContent()
    val content = AttachmentContent.Binary.Bytes(bytes)

    return when (type) {
        Messaging.Attachment.Type.Audio -> KoogContentPart.Audio(
            content = content,
            format = name.substringAfterLast('.'),
            mimeType = mimeType,
            fileName = name,
        )

        Messaging.Attachment.Type.Image -> KoogContentPart.Image(
            content = content,
            format = name.substringAfterLast('.'),
            mimeType = mimeType,
            fileName = name,
        )

        Messaging.Attachment.Type.Video -> KoogContentPart.Video(
            content = content,
            format = name.substringAfterLast('.'),
            mimeType = mimeType,
            fileName = name,
        )

        Messaging.Attachment.Type.Document -> KoogContentPart.File(
            content = content,
            format = name.substringAfterLast('.'),
            mimeType = mimeType,
            fileName = name,
        )
    }
}