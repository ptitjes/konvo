package io.github.ptitjes.konvo.core.settings

import kotlinx.serialization.*

/**
 * Settings for application appearance.
 */
@Serializable
data class AppearanceSettings(
    val baseColorScheme: BaseColorScheme = BaseColorScheme.System,
)

/**
 * Base color scheme preference.
 */
@Serializable
enum class BaseColorScheme {
    Dark, Light, System
}

/**
 * Key for appearance settings persisted in the configuration directory.
 */
val AppearanceSettingsKey: SettingsSectionKey<AppearanceSettings> = SettingsSectionKey(
    name = "appearance",
    defaultValue = AppearanceSettings(),
    serializer = AppearanceSettings.serializer(),
)
