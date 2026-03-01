package io.github.ptitjes.konvo.core.agents.toolkit

import ai.koog.agents.core.agent.entity.AIAgentStorageKey
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.dsl.PromptBuilder
import io.github.ptitjes.konvo.core.agents.AgentContext
import io.github.ptitjes.konvo.core.mcp.McpHostSession
import kotlin.coroutines.CoroutineContext

class DefaultAgentContext(
    private val coroutineContext: CoroutineContext,
    private val mcpSessionFactory: ((coroutineContext: CoroutineContext) -> McpHostSession)? = null,
    private val initialPrompt: () -> Prompt,
) : AgentContext {
    // TODO make atomic
    override var prompt: Prompt = initialPrompt()
        private set

    override fun resetPrompt(prompt: Prompt) {
        this.prompt = prompt
    }

    override fun resetPrompt(builder: PromptBuilder.() -> Unit) {
        prompt = Prompt.build(initialPrompt()) { builder() }
    }

    override fun appendToPrompt(builder: PromptBuilder.() -> Unit) {
        prompt = Prompt.build(prompt) { builder() }
    }

    override suspend fun <R> withMcpSession(block: suspend (McpHostSession) -> R): R =
        block(getOrCreateSession())

    // TODO make atomic
    private var mcpSession: McpHostSession? = null

    private fun getOrCreateSession(): McpHostSession = mcpSession
        ?: mcpSessionFactory?.invoke(coroutineContext).also { mcpSession = it }
        ?: error("MCP session not available")

    val isPaused: Boolean
        get() = TODO("Not yet implemented")

    suspend fun pause() {
        TODO("Not yet implemented")
    }

    suspend fun resume() {
        TODO("Not yet implemented")
    }

    suspend fun close() {
        mcpSession?.close()
        mcpSession = null
    }
}
