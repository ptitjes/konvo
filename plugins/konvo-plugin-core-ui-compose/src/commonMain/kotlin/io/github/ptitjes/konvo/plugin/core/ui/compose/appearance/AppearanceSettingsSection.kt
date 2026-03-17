package io.github.ptitjes.konvo.plugin.core.ui.compose.appearance

import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import io.github.ptitjes.syrup.specification.*

fun PluginSpecificationBuilder.appearanceSettings() {
    SettingsSections {
        contribution {
            SettingsSection(
                titleKey = "appearance",
                icon = Res.drawable.ic_palette,
                title = { strings.settings.sectionTitles.getValue("appearance") },
                panel = { AppearanceSettingsPanel() },
            )
        }
    }

    i18nStrings<AppearanceStrings>("ar-SA") { ArStrings.appearance }
    i18nStrings<AppearanceStrings>("en-US") { EnStrings.appearance }
    i18nStrings<AppearanceStrings>("es-ES") { EsStrings.appearance }
    i18nStrings<AppearanceStrings>("fr-FR") { FrStrings.appearance }
    i18nStrings<AppearanceStrings>("hi-IN") { HiStrings.appearance }
    i18nStrings<AppearanceStrings>("zh-CN") { ZhStrings.appearance }
}
