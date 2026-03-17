package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import io.github.ptitjes.syrup.specification.*

internal val I18nStrings.settings: SettingsStrings get() = byType()

internal data class SettingsStrings(
    // Left list panel
    val listTitle: String,
    val selectSectionAria: String,
)

fun PluginSpecificationBuilder.settingsStrings() {
    i18nStrings<SettingsStrings>("ar-SA") { ArStrings.settings }
    i18nStrings<SettingsStrings>("en-US") { EnStrings.settings }
    i18nStrings<SettingsStrings>("es-ES") { EsStrings.settings }
    i18nStrings<SettingsStrings>("fr-FR") { FrStrings.settings }
    i18nStrings<SettingsStrings>("hi-IN") { HiStrings.settings }
    i18nStrings<SettingsStrings>("zh-CN") { ZhStrings.settings }
}
