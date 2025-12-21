package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlin.reflect.*

sealed interface Messaging : Event.Payload {

    data class Message(
        val content: List<Part>,
    ) : Messaging, Event.Agent, Event.User

    sealed interface Part {
        data class Text(
            val text: String,
            val mimeType: String? = null,
        ) : Part

        sealed interface Media : Part {
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
        ) : Part
    }

    data class Attachment(
        val type: Type,
        val url: String,
        val name: String,
        val mimeType: String,
    ) {
        enum class Type {
            Audio, Image, Video, Document,
        }
    }
}
