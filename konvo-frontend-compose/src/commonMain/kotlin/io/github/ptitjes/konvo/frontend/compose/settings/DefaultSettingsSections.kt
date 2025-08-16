package io.github.ptitjes.konvo.frontend.compose.settings

import io.github.ptitjes.konvo.core.settings.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*

val defaultSettingsSections = listOf(
    SettingsSection(
        key = AppearanceSettingsKey,
        title = "Appearance",
        panel = ::AppearanceSettingsPanel,
    ),
    SettingsSection(
        key = ModelProviderSettingsKey,
        title = "Model providers",
        panel = ::ModelProviderSettingsPanel,
    ),
)
