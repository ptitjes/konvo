package io.github.ptitjes.konvo.plugin.core.ui.compose.prompts

import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import io.github.ptitjes.syrup.specification.*

internal val I18nStrings.prompts: PromptStrings get() = byType()

/**
 * Translated strings for the prompts package (prompt selector UI).
 */
internal data class PromptStrings(
    val selectorLabel: String,
)

fun PluginSpecificationBuilder.promptStrings() {
    i18nStrings<PromptStrings>("ar-SA") { ArStrings.prompts }
    i18nStrings<PromptStrings>("en-US") { EnStrings.prompts }
    i18nStrings<PromptStrings>("es-ES") { EsStrings.prompts }
    i18nStrings<PromptStrings>("fr-FR") { FrStrings.prompts }
    i18nStrings<PromptStrings>("hi-IN") { HiStrings.prompts }
    i18nStrings<PromptStrings>("zh-CN") { ZhStrings.prompts }
}
