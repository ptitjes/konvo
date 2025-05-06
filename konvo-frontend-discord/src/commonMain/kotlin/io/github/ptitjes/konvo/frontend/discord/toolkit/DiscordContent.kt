package io.github.ptitjes.konvo.frontend.discord.toolkit

private const val CONTENT_MAX_LENGTH = 2000
private const val LABEL_MAX_LENGTH = 100

fun String.maybeSplitDiscordContent(): List<String> {
    if (length <= CONTENT_MAX_LENGTH) return listOf(this)
    else {
        var remaining = this
        val split = mutableListOf<String>()
        do {
            val index = remaining.lastIndexOf('\n', CONTENT_MAX_LENGTH)
            split.add(remaining.substring(0, index).trim())
            remaining = remaining.substring(index + 1).trim()
        } while (remaining.length > CONTENT_MAX_LENGTH)
        split.add(remaining)
        return split
    }
}

fun String.maybeEllipsisDiscordLabel(): String {
    return (if (length <= LABEL_MAX_LENGTH) this else substring(0, LABEL_MAX_LENGTH - 3) + "...")
}

fun String.maybeEllipsisDiscordContent(): String {
    return (if (length <= CONTENT_MAX_LENGTH) this else substring(0, CONTENT_MAX_LENGTH - 3) + "...")
}
