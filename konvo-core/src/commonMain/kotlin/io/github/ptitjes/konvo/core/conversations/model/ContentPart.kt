package io.github.ptitjes.konvo.core.conversations.model

import kotlin.reflect.*

sealed interface ContentPart {
    data class Text(
        val text: String,
        val mimeType: String? = null,
    ) : ContentPart

    sealed interface Media : ContentPart {
        val mimeType: String
        val filename: String?
        val media: Attachment
    }

    data class Image(
        override val mimeType: String,
        override val filename: String?,
        override val media: Attachment,
    ) : Media

    data class Video(
        override val mimeType: String,
        override val filename: String?,
        override val media: Attachment,
    ) : Media

    data class Audio(
        override val mimeType: String,
        override val filename: String?,
        override val media: Attachment,
    ) : Media

    data class File(
        override val mimeType: String,
        override val filename: String?,
        override val media: Attachment,
    ) : Media

    data class Embed<Payload>(
        val type: KType,
        val payload: Payload,
    ) : ContentPart
}
