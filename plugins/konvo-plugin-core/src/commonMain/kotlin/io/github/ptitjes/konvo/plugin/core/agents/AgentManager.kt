package io.github.ptitjes.konvo.plugin.core.agents

import io.github.ptitjes.syrup.*
import kotlin.reflect.*

class AgentManager(
    pluginContext: PluginContext,
) {
    private val agents by pluginContext.contributions(Agents)

    fun getAgent(configurationClass: KClass<out AgentConfiguration>): Agent {
        return agents.firstOrNull { it.configurationClass == configurationClass }
            ?: error("No agent found for configuration class: ${configurationClass.simpleName}")
    }
}
