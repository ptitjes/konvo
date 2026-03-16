package io.github.ptitjes.konvo.plugin.core.settings

import kotlinx.serialization.*

/**
 * Developer settings for the application.
 */
@Serializable
data class DeveloperSettings(
    /**
     * OpenTelemetry settings.
     */
    val openTelemetry: OpenTelemetry = OpenTelemetry(),
) {
    /**
     * OpenTelemetry settings.
     */
    @Serializable
    data class OpenTelemetry(
        /**
         * Whether OpenTelemetry export is enabled.
         */
        val enabled: Boolean = false,

        /**
         * The endpoint for OpenTelemetry export.
         */
        val endpoint: String = "http://localhost:4317",

        /**
         * Whether OpenTelemetry export is verbose.
         */
        val verbose: Boolean = false,
    )
}

/**
 * Key for developer settings persisted in the configuration directory.
 */
val DeveloperSettingsKey: SettingsKey<DeveloperSettings> = SettingsKey(
    name = "developer",
    defaultValue = DeveloperSettings(),
    serializer = DeveloperSettings.serializer(),
)
