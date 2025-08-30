package io.github.ptitjes.konvo.core.conversations.model

/**
 * Formatting helpers used by the Compose UI.
 */
object TextFormatters {
    /**
     * Truncate a preview text to at most [maxChars] characters, preserving whole words when possible
     * and appending an ellipsis when truncation occurs. Collapses internal whitespace and strips Markdown formatting and images.
     */
    fun truncatePreview(text: String, maxChars: Int): String {
        val plain = stripMarkdown(text)
        val normalized = plain.replace("\n", " ").replace(WHITESPACE_REGEX, " ").trim()
        if (normalized.length <= maxChars) return normalized
        val cut = normalized.take(maxChars)
        val lastSpace = cut.lastIndexOf(' ')
        val base = if (lastSpace > maxChars * 2 / 3) cut.take(lastSpace) else cut
        val result = base.trimEnd() + "…"
        println(result)
        return result
    }

    private fun stripMarkdown(input: String): String {
        var stripped = input
        // Remove Markdown images ![alt](url)
        stripped = MD_IMAGE_REGEX.replace(stripped, "")
        // Remove HTML <img ...> tags
        stripped = HTML_IMG_TAG_REGEX.replace(stripped, "")
        // Convert links [text](url) -> text
        stripped = MD_LINK_REGEX.replace(stripped, "$1")
        // Remove fenced code block markers ```lang? ... ``` while keeping inner content
        stripped = FENCED_CODE_REGEX.replace(stripped) { m ->
            val value = m.value
            val inner = if (value.length >= 6) value.substring(3, value.length - 3) else ""
            inner
        }
        // Inline code `code` -> code
        stripped = INLINE_CODE_REGEX.replace(stripped, "$1")
        // Headers at line start: #, ##, ...
        stripped = HEADER_REGEX.replace(stripped, "")
        // Blockquotes >
        stripped = BLOCKQUOTE_REGEX.replace(stripped, "")
        // Unordered list markers -, *, + at line start
        stripped = ULIST_REGEX.replace(stripped, "")
        // Ordered list markers 1. 2. ... at line start
        stripped = OLIST_REGEX.replace(stripped, "")
        // Bold/strong **text** or __text__
        stripped = BOLD_REGEX.replace(stripped, "$2")
        // Italic *text* or _text_
        stripped = ITALIC_REGEX.replace(stripped, "$2")
        // Strikethrough ~~text~~
        stripped = STRIKE_REGEX.replace(stripped, "$1")
        return stripped
    }

    // Precompiled regexes for performance and readability
    private val WHITESPACE_REGEX = Regex("\\s+")
    private val MD_IMAGE_REGEX = Regex("!\\[[^\\]]*]\\([^)]*\\)")
    private val HTML_IMG_TAG_REGEX = Regex("<img\\b[^>]*>", setOf(RegexOption.IGNORE_CASE))
    private val MD_LINK_REGEX = Regex("\\[([^\\]]+)]\\(([^)]+)\\)")
    private val FENCED_CODE_REGEX = Regex("```[\\s\\S]*?```", setOf(RegexOption.MULTILINE))
    private val INLINE_CODE_REGEX = Regex("`([^`]*)`")
    private val HEADER_REGEX = Regex("^\\s{0,3}#{1,6}\\s*", setOf(RegexOption.MULTILINE))
    private val BLOCKQUOTE_REGEX = Regex("^\\s*>\\s?", setOf(RegexOption.MULTILINE))
    private val ULIST_REGEX = Regex("^\\s*[\\-*+]\\s+", setOf(RegexOption.MULTILINE))
    private val OLIST_REGEX = Regex("^\\s*\\d+\\.\\s+", setOf(RegexOption.MULTILINE))
    private val BOLD_REGEX = Regex("(\\*\\*|__)(.*?)\\1", setOf(RegexOption.DOT_MATCHES_ALL))
    private val ITALIC_REGEX = Regex("(\\*|_)(.*?)\\1", setOf(RegexOption.DOT_MATCHES_ALL))
    private val STRIKE_REGEX = Regex("~~(.*?)~~", setOf(RegexOption.DOT_MATCHES_ALL))
}
