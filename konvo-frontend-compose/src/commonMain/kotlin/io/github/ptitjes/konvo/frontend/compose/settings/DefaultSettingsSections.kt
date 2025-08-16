package io.github.ptitjes.konvo.frontend.compose.settings

import io.github.ptitjes.konvo.core.settings.AppearanceSettingsKey
import io.github.ptitjes.konvo.frontend.compose.viewmodels.SettingsSection

val defaultSettingsSections = listOf(
    SettingsSection(
        key = AppearanceSettingsKey,
        title = "Appearance",
        panel = ::AppearanceSettingsPanel,
    )
)
