package io.github.ptitjes.konvo.core.conversations.model

import kotlin.reflect.*

sealed interface ContentPart {
    data class Text(
        val text: String,
        val mimeType: String? = null,
    ) : ContentPart

    sealed interface Media : ContentPart

    data class Image(
        val mimeType: String,
        val filename: String?,
        val media: Attachment,
    ) : Media

    data class Video(
        val mimeType: String,
        val filename: String?,
        val media: Attachment,
    ) : Media

    data class Audio(
        val mimeType: String,
        val filename: String?,
        val media: Attachment,
    ) : Media

    data class Embed<Payload>(
        val type: KType,
        val payload: Payload,
    ) : ContentPart
}
