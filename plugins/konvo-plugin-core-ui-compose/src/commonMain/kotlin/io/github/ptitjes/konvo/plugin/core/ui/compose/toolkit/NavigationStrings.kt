package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit

import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import io.github.ptitjes.syrup.specification.*

internal val I18nStrings.navigation: NavigationStrings get() = byType()

data class NavigationStrings(
    val navigationOpenAria: String,
    val navigationCloseAria: String,
    val backAria: String,
    val detailsOpenAria: String,
    val detailsCloseAria: String,
)

fun PluginSpecificationBuilder.navigationStrings() {
    i18nStrings<NavigationStrings>("ar-SA") { ArStrings.navigation }
    i18nStrings<NavigationStrings>("en-US") { EnStrings.navigation }
    i18nStrings<NavigationStrings>("es-ES") { EsStrings.navigation }
    i18nStrings<NavigationStrings>("fr-FR") { FrStrings.navigation }
    i18nStrings<NavigationStrings>("hi-IN") { HiStrings.navigation }
    i18nStrings<NavigationStrings>("zh-CN") { ZhStrings.navigation }
}
