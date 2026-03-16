package io.github.ptitjes.konvo.plugin.core.agents

import io.github.ptitjes.konvo.plugin.core.agents.toolkit.*

class AgentFactory(
    private val agents: Set<InteractiveAgent<*>>,
) {
    fun createAgent(agentConfiguration: AgentConfiguration): Agent {
        val configurationClass = agentConfiguration::class

        return agents.firstOrNull { it.configurationClass == configurationClass }
            ?: error("No agent found for configuration class: ${configurationClass.simpleName}")
    }
}
