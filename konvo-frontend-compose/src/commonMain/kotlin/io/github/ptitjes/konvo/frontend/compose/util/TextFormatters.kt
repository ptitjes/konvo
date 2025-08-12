package io.github.ptitjes.konvo.frontend.compose.util

import kotlin.math.*
import kotlin.time.*

/**
 * Formatting helpers used by the Compose UI.
 */
object TextFormatters {
    /**
     * Format a timestamp into a short, human-friendly string for lists.
     * - If within the last minute: "now"
     * - If within the last hour: "Xm"
     * - If within the last day: "Xh"
     * - Else: "YYYY-MM-DD"
     */
    @OptIn(ExperimentalTime::class)
    fun formatTimestampRelative(
        instant: Instant,
        nowProvider: () -> Instant = { Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds()) },
    ): String {
        val now = nowProvider()
        val diffMillis = (now.toEpochMilliseconds() - instant.toEpochMilliseconds()).absoluteValue
        val minute = 60_000L
        val hour = 60L * minute
        val day = 24L * hour

        return when {
            diffMillis < minute -> "now"
            diffMillis < hour -> "${diffMillis / minute}m"
            diffMillis < day -> "${diffMillis / hour}h"
            else -> {
                val iso = instant.toString()
                if (iso.length >= 10) iso.substring(0, 10) else iso
            }
        }
    }

    /**
     * Truncate a preview text to at most [maxChars] characters, preserving whole words when possible
     * and appending an ellipsis when truncation occurs. Collapses internal whitespace.
     */
    fun truncatePreview(text: String, maxChars: Int = 140): String {
        val normalized = text.replace("\n", " ").replace(Regex("\\s+"), " ").trim()
        if (normalized.length <= maxChars) return normalized
        val cut = normalized.substring(0, maxChars)
        val lastSpace = cut.lastIndexOf(' ')
        val base = if (lastSpace > maxChars * 2 / 3) cut.substring(0, lastSpace) else cut
        return base.trimEnd() + "…"
    }
}
