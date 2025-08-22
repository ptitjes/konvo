package io.github.ptitjes.konvo.frontend.compose.toolkit.text

import androidx.compose.runtime.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import kotlinx.datetime.*
import nl.jacobras.humanreadable.*
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
internal fun rememberRelativeTimestampFormatter(): RelativeTimestampFormatter {
    val formats = strings.formats
    return remember { RelativeTimestampFormatter(formats) }
}

internal class RelativeTimestampFormatter(private val formats: FormatStrings) {
    fun format(instant: Instant, nowProvider: () -> Instant = { Clock.System.now() }): String {
        val duration = nowProvider() - instant
        val secondsAgo = duration.inWholeSeconds.toInt()
        val daysAgo = duration.inWholeDays.toInt()
        return when {
            secondsAgo < 60 -> formats.now
            secondsAgo >= 60 && daysAgo < 30 -> HumanReadable.duration(duration)
            else -> LocalDate.Formats.ISO.format(instant.toLocalDateTime(TimeZone.currentSystemDefault()).date)
        }
    }
}

internal data class FormatStrings(
    val now: String,
)
