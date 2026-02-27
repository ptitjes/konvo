package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.serialization.*
import kotlin.reflect.*

@Serializable
sealed interface Messaging : Action.Payload {

    @Serializable
    @SerialName("message")
    data class Message(
        val content: List<Part>,
    ) : Messaging, Action.Agent, Action.User

    @Serializable
    sealed interface Part {
        @Serializable
        @SerialName("text")
        data class Text(
            val text: String,
            val mimeType: String? = null,
        ) : Part

        @Serializable
        sealed interface Media : Part {
            val mimeType: String
            val filename: String?
            val media: Attachment
        }

        @Serializable
        @SerialName("image")
        data class Image(
            override val mimeType: String,
            override val filename: String?,
            override val media: Attachment,
        ) : Media

        @Serializable
        @SerialName("video")
        data class Video(
            override val mimeType: String,
            override val filename: String?,
            override val media: Attachment,
        ) : Media

        @Serializable
        @SerialName("audio")
        data class Audio(
            override val mimeType: String,
            override val filename: String?,
            override val media: Attachment,
        ) : Media

        @Serializable
        @SerialName("file")
        data class File(
            override val mimeType: String,
            override val filename: String?,
            override val media: Attachment,
        ) : Media

        // TODO: Embed payload serialization?
        data class Embed<Payload>(
            val type: KType,
            val payload: Payload,
        ) : Part
    }

    @Serializable
    data class Attachment(
        val type: Type,
        val url: String,
        val name: String,
        val mimeType: String,
    ) {
        @Serializable
        enum class Type {
            @SerialName("audio") Audio,
            @SerialName("image") Image,
            @SerialName("video") Video,
            @SerialName("document") Document,
        }
    }
}
