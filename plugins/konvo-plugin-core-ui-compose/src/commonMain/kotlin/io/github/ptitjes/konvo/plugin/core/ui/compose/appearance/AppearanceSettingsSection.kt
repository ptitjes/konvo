package io.github.ptitjes.konvo.plugin.core.ui.compose.appearance

import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import io.github.ptitjes.syrup.specification.*
import org.kodein.di.*

fun PluginSpecificationBuilder.appearanceSettings() {
    SettingsSections {
        contribution {
            SettingsSection(
                titleKey = "appearance",
                icon = Res.drawable.ic_palette,
                title = { i18n.appearance.settingsTitle },
                presenterFactory = { new(::AppearanceSettingsPresenter) },
                panel = { state -> AppearanceSettingsPanel(state) },
            )
        }
    }

    appearanceStrings()
}

private fun PluginSpecificationBuilder.appearanceStrings() {
    i18nStrings<AppearanceStrings>("ar-SA") { ArStrings.appearance }
    i18nStrings<AppearanceStrings>("en-US") { EnStrings.appearance }
    i18nStrings<AppearanceStrings>("es-ES") { EsStrings.appearance }
    i18nStrings<AppearanceStrings>("fr-FR") { FrStrings.appearance }
    i18nStrings<AppearanceStrings>("hi-IN") { HiStrings.appearance }
    i18nStrings<AppearanceStrings>("zh-CN") { ZhStrings.appearance }
}
