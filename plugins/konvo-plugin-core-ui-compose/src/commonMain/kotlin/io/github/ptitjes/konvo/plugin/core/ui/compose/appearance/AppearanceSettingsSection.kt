package io.github.ptitjes.konvo.plugin.core.ui.compose.appearance

import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.syrup.specification.*

fun PluginSpecificationBuilder.appearanceSettingsSection() {
    SettingsSections {
        contribution {
            SettingsSection(
                titleKey = "appearance",
                icon = Res.drawable.ic_palette,
                panel = { AppearanceSettingsPanel() },
            )
        }
    }
}
