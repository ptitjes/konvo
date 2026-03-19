package io.github.ptitjes.konvo.plugin.core.ui.compose.agents

import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import io.github.ptitjes.syrup.specification.*

internal val I18nStrings.agents: AgentStrings get() = byType()

data class AgentStrings(
    val questionAnswerDisplayName: String,
)

fun PluginSpecificationBuilder.agentStrings() {
    i18nStrings<AgentStrings>("ar-SA") { ArStrings.agents }
    i18nStrings<AgentStrings>("en-US") { EnStrings.agents }
    i18nStrings<AgentStrings>("es-ES") { EsStrings.agents }
    i18nStrings<AgentStrings>("fr-FR") { FrStrings.agents }
    i18nStrings<AgentStrings>("hi-IN") { HiStrings.agents }
    i18nStrings<AgentStrings>("zh-CN") { ZhStrings.agents }
}
