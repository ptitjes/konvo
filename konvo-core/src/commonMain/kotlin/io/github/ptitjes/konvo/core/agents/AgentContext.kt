package io.github.ptitjes.konvo.core.agents

import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.dsl.PromptBuilder
import io.github.ptitjes.konvo.core.mcp.McpHostSession

interface AgentContext {
    fun <S> loadState(key: AgentStateKey<S>): S?
    fun <S> updateState(key: AgentStateKey<S>, state: S)
    fun <S> updateState(key: AgentStateKey<S>, updater: (previous: S?) -> S)

    val prompt: Prompt
    fun resetPrompt(prompt: Prompt)
    fun resetPrompt(builder: PromptBuilder.() -> Unit)
    fun appendToPrompt(builder: PromptBuilder.() -> Unit)

    suspend fun <T> withMcpSession(block: suspend (McpHostSession) -> T): T
}
