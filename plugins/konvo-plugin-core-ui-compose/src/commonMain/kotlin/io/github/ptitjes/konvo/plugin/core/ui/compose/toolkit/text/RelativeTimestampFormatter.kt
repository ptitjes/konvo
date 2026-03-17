package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.text

import androidx.compose.runtime.*
import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import io.github.ptitjes.syrup.specification.*
import kotlinx.datetime.*
import nl.jacobras.humanreadable.*
import kotlin.time.Clock
import kotlin.time.Instant

internal val I18nStrings.formats: FormatStrings get() = byType()

@Composable
internal fun rememberRelativeTimestampFormatter(): RelativeTimestampFormatter {
    val formats = i18n.formats
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

fun PluginSpecificationBuilder.formatStrings() {
    i18nStrings<FormatStrings>("ar-SA") { ArStrings.formats }
    i18nStrings<FormatStrings>("en-US") { EnStrings.formats }
    i18nStrings<FormatStrings>("es-ES") { EsStrings.formats }
    i18nStrings<FormatStrings>("fr-FR") { FrStrings.formats }
    i18nStrings<FormatStrings>("hi-IN") { HiStrings.formats }
    i18nStrings<FormatStrings>("zh-CN") { ZhStrings.formats }
}
