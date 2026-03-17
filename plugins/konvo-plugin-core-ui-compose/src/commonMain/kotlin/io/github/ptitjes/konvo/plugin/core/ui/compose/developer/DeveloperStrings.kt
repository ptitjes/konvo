package io.github.ptitjes.konvo.plugin.core.ui.compose.developer

import io.github.ptitjes.konvo.plugin.core.i18n.*

val I18nStrings.developer: DeveloperStrings get() = byType()

/**
 * Translated strings for the developer package.
 */
data class DeveloperStrings(
    val openTelemetryTitle: String,
    val openTelemetryDescription: String,
    val openTelemetryEnabledLabel: String,
    val openTelemetryEndpointLabel: String,
    val openTelemetryVerboseLabel: String,
)
