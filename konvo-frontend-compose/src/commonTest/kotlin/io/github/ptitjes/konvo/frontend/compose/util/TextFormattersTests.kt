package io.github.ptitjes.konvo.frontend.compose.util

import kotlin.test.*
import kotlin.time.*

class TextFormattersTests {

    @OptIn(ExperimentalTime::class)
    @Test
    fun `truncatePreview collapses whitespace and adds ellipsis when needed`() {
        val text = "This   is a\nlong   preview   that should be truncated at some point because it exceeds the maximum length allowed by the UI."
        val result = TextFormatters.truncatePreview(text, maxChars = 50)
        // Should not exceed 51 including ellipsis
        assert(result.length <= 51)
        // Should end with ellipsis
        assert(result.endsWith("…"))
        // Should have single spaces
        assert(!result.contains("  "))
    }

    @Test
    @OptIn(ExperimentalTime::class)
    fun `formatTimestampRelative returns minutes and hours for recent times`() {
        val base = kotlin.time.Instant.fromEpochMilliseconds(1_000_000L)
        val oneMinuteLater = kotlin.time.Instant.fromEpochMilliseconds(1_000_000L + 60_000L)
        val fiftyNineMinutesLater = kotlin.time.Instant.fromEpochMilliseconds(1_000_000L + 59 * 60_000L)
        val twoHoursLater = kotlin.time.Instant.fromEpochMilliseconds(1_000_000L + 2 * 60L * 60_000L)

        assertEquals("now", TextFormatters.formatTimestampRelative(base) { base })
        assertEquals("1m", TextFormatters.formatTimestampRelative(base) { oneMinuteLater })
        assertEquals("59m", TextFormatters.formatTimestampRelative(base) { fiftyNineMinutesLater })
        assertEquals("2h", TextFormatters.formatTimestampRelative(base) { twoHoursLater })
    }

    @Test
    @OptIn(ExperimentalTime::class)
    fun `formatTimestampRelative returns ISO date for older times`() {
        val base = kotlin.time.Instant.fromEpochMilliseconds(0L)
        val twoDaysLater = kotlin.time.Instant.fromEpochMilliseconds(2 * 24L * 60L * 60L * 1000L)
        val formatted = TextFormatters.formatTimestampRelative(base) { twoDaysLater }
        // Expect YYYY-MM-DD
        assertEquals("1970-01-01", formatted)
    }
}
