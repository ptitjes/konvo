package io.github.ptitjes.konvo.core.conversations.model

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
