package io.github.ptitjes.konvo.plugin.core.agents

class AgentStateKey<S>(val name: String) {
    override fun toString(): String = "${super.toString()}(name=$name)"
}

fun <S> createAgentStateKey(name: String): AgentStateKey<S> = AgentStateKey(name)
