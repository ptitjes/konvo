package io.github.ptitjes.konvo.frontend.discord.toolkit

import dev.kord.rest.*
import dev.kord.rest.builder.message.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

suspend fun MessageBuilder.addCharacterAvatarFile(
    httpClient: HttpClient,
    characterUrl: String,
    characterName: String,
) {
    val response = httpClient.get(characterUrl)
    val contentType = response.contentType()
    val extension = contentType?.let { contentTypeToExtensionOrNull(it) }
    if (extension != null) {
        val imageChannel = response.bodyAsChannel()
        addSpoilerFile(
            name = "${characterName}-avatar.${extension}",
            contentProvider = ChannelProvider { imageChannel },
        ) {
            description = "${characterName}'s picture"
        }
    }
}

private fun contentTypeToExtensionOrNull(contentType: ContentType): String? = when (contentType) {
    ContentType.Image.GIF -> "gif"
    ContentType.Image.JPEG -> "jpeg"
    ContentType.Image.PNG -> "png"
    else -> null
}

inline fun MessageBuilder.addSpoilerFile(
    name: String,
    contentProvider: ChannelProvider,
    builder: AttachmentBuilder.() -> Unit,
): NamedFile = addFile("SPOILER_$name", contentProvider, builder)
