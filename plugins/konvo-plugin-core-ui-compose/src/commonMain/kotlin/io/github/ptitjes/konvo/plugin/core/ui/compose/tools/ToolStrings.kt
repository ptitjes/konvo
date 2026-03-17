package io.github.ptitjes.konvo.plugin.core.ui.compose.tools

import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import io.github.ptitjes.syrup.specification.*

internal val I18nStrings.tools: ToolStrings get() = byType()

/**
 * Translated strings for the tools package (tools selector UI).
 */
internal data class ToolStrings(
    val panelLabel: String,
    val emptyMessage: String,
)

fun PluginSpecificationBuilder.toolStrings() {
    i18nStrings<ToolStrings>("ar-SA") { ArStrings.tools }
    i18nStrings<ToolStrings>("en-US") { EnStrings.tools }
    i18nStrings<ToolStrings>("es-ES") { EsStrings.tools }
    i18nStrings<ToolStrings>("fr-FR") { FrStrings.tools }
    i18nStrings<ToolStrings>("hi-IN") { HiStrings.tools }
    i18nStrings<ToolStrings>("zh-CN") { ZhStrings.tools }
}
