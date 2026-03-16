package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

internal data class SettingsStrings(
    // Left list panel
    val listTitle: String,
    val selectSectionAria: String,

    // Section titles
    val sectionTitles: Map<String, String>,

    // Appearance settings
    val appearanceBaseColorSchemeTitle: String,
    val appearanceBaseColorSchemeDescription: String,
    val appearanceBaseColorSchemeOptionDark: String,
    val appearanceBaseColorSchemeOptionLight: String,
    val appearanceBaseColorSchemeOptionSystem: String,

    // Developer settings
    val developerOpenTelemetryTitle: String,
    val developerOpenTelemetryDescription: String,
    val developerOpenTelemetryEnabledLabel: String,
    val developerOpenTelemetryEndpointLabel: String,
    val developerOpenTelemetryVerboseLabel: String,
)
