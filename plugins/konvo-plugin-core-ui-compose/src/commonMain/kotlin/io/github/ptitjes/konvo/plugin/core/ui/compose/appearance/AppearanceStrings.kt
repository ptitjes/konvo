package io.github.ptitjes.konvo.plugin.core.ui.compose.appearance

import io.github.ptitjes.konvo.plugin.core.i18n.*

internal val I18nStrings.appearance: AppearanceStrings get() = byType()

/**
 * Translated strings for the appearance package.
 */
data class AppearanceStrings(
    val settingsTitle: String,
    val baseColorSchemeTitle: String,
    val baseColorSchemeDescription: String,
    val baseColorSchemeOptionDark: String,
    val baseColorSchemeOptionLight: String,
    val baseColorSchemeOptionSystem: String,
)
