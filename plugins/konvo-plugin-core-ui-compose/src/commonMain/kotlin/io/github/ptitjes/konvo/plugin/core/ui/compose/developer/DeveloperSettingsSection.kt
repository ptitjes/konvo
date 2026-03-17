package io.github.ptitjes.konvo.plugin.core.ui.compose.developer

import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import io.github.ptitjes.syrup.specification.*

fun PluginSpecificationBuilder.developerSettings() {
    SettingsSections {
        contribution {
            SettingsSection(
                titleKey = "developer",
                icon = Res.drawable.ic_mobile_code,
                title = { strings.developer.settingsTitle },
                panel = { DeveloperSettingsPanel() },
            )
        }
    }

    developerStrings()
}

private fun PluginSpecificationBuilder.developerStrings() {
    i18nStrings<DeveloperStrings>("ar-SA") { ArStrings.developer }
    i18nStrings<DeveloperStrings>("en-US") { EnStrings.developer }
    i18nStrings<DeveloperStrings>("es-ES") { EsStrings.developer }
    i18nStrings<DeveloperStrings>("fr-FR") { FrStrings.developer }
    i18nStrings<DeveloperStrings>("hi-IN") { HiStrings.developer }
    i18nStrings<DeveloperStrings>("zh-CN") { ZhStrings.developer }
}
