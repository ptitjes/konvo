package io.github.ptitjes.konvo.frontend.compose.toolkit.text

import kotlinx.datetime.*
import nl.jacobras.humanreadable.*
import kotlin.time.*
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Formatting helpers used by the Compose UI.
 */
object TextFormatters {
    /**
     * Format a timestamp into a short, human-friendly string for lists.
     * - If within the last minute: "now"
     * - If within the last hour: "X minutes"
     * - If within the last day: "X hours"
     * - If within the last week: "X days"
     * - If within the last month: "X weeks"
     * - Else: "YYYY-MM-DD"
     */
    @OptIn(ExperimentalTime::class)
    fun formatTimestampRelative(
        instant: Instant,
        nowProvider: () -> Instant = { Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds()) },
    ): String {
        val duration = nowProvider() - instant
        val secondsAgo = duration.inWholeSeconds.toInt()
        val daysAgo = duration.inWholeDays.toInt()
        return when {
            secondsAgo < 60 -> "now"
            secondsAgo >= 60 && daysAgo < 30 -> HumanReadable.duration(duration)
            else -> LocalDate.Formats.ISO.format(instant.toLocalDateTime(TimeZone.currentSystemDefault()).date)
        }
    }

    /**
     * Truncate a preview text to at most [maxChars] characters, preserving whole words when possible
     * and appending an ellipsis when truncation occurs. Collapses internal whitespace and strips Markdown formatting and images.
     */
    fun truncatePreview(text: String, maxChars: Int = 140): String {
        val plain = stripMarkdown(text)
        val normalized = plain.replace("\n", " ").replace(WHITESPACE_REGEX, " ").trim()
        if (normalized.length <= maxChars) return normalized
        val cut = normalized.substring(0, maxChars)
        val lastSpace = cut.lastIndexOf(' ')
        val base = if (lastSpace > maxChars * 2 / 3) cut.substring(0, lastSpace) else cut
        return base.trimEnd() + "…"
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
