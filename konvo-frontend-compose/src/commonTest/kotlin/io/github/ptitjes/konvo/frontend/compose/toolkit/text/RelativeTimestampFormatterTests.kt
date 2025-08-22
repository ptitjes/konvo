package io.github.ptitjes.konvo.frontend.compose.toolkit.text

import androidx.compose.ui.test.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import kotlin.test.*
import kotlin.time.*

class RelativeTimestampFormatterTests {
    @Test
    fun `formatTimestampRelative returns minutes and hours for recent times`() = runComposeUiTest {
        setContent {
            ProvideStrings(rememberStrings(currentLanguageTag = "en-US")) {
                val timestampFormatter = rememberRelativeTimestampFormatter()

                val base = Instant.fromEpochMilliseconds(0L)
                val oneMinuteLater = Instant.fromEpochMilliseconds(60_000L)
                val fiftyNineMinutesLater = Instant.fromEpochMilliseconds(59 * 60_000L)
                val twoHoursLater = Instant.fromEpochMilliseconds(2 * 60L * 60_000L)
                val twoDaysLater = Instant.fromEpochMilliseconds(2 * 24L * 60L * 60L * 1000L)
                val twoWeeksLater = Instant.fromEpochMilliseconds(2 * 7 * 24L * 60L * 60L * 1000L)

                assertEquals("now", timestampFormatter.format(base) { base })
                assertEquals("1 minute", timestampFormatter.format(base) { oneMinuteLater })
                assertEquals("59 minutes", timestampFormatter.format(base) { fiftyNineMinutesLater })
                assertEquals("2 hours", timestampFormatter.format(base) { twoHoursLater })
                assertEquals("2 days", timestampFormatter.format(base) { twoDaysLater })
                assertEquals("2 weeks", timestampFormatter.format(base) { twoWeeksLater })
            }
        }
    }

    @Test
    fun `formatTimestampRelative returns ISO date for older times`() = runComposeUiTest {
        setContent {
            ProvideStrings(rememberStrings(currentLanguageTag = "en-US")) {
                val timestampFormatter = rememberRelativeTimestampFormatter()
                val base = Instant.fromEpochMilliseconds(0L)
                val sixtyDaysLater = Instant.fromEpochMilliseconds(60 * 24L * 60L * 60L * 1000L)
                val formatted = timestampFormatter.format(base) { sixtyDaysLater }
                assertEquals("1970-01-01", formatted)
            }
        }
    }
}
