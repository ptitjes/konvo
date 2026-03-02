package io.github.ptitjes.konvo.core.agents.toolkit

import ai.koog.prompt.dsl.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.mcp.*
import kotlin.concurrent.atomics.*
import kotlin.coroutines.*

@OptIn(ExperimentalAtomicApi::class)
class DefaultAgentContext(
    private val coroutineContext: CoroutineContext,
    private val mcpSessionFactory: ((coroutineContext: CoroutineContext) -> McpHostSession)? = null,
    private val initialPrompt: () -> Prompt,
) : AgentContext {
    private val stateRey = AtomicReference<Map<AgentStateKey<*>, Any?>>(emptyMap())

    @Suppress("UNCHECKED_CAST")
    override fun <S> loadState(key: AgentStateKey<S>): S? = stateRey.load()[key] as S?

    override fun <S> updateState(key: AgentStateKey<S>, state: S) {
        this.stateRey.update { it + (key to state) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S> updateState(key: AgentStateKey<S>, updater: (previous: S?) -> S) {
        this.stateRey.update { it + (key to updater(it[key] as S?)) }
    }

    private val promptRef = AtomicReference(initialPrompt())

    override var prompt: Prompt
        get() = promptRef.load()
        private set(value) {
            promptRef.exchange(value)
        }

    override fun resetPrompt(prompt: Prompt) {
        this.prompt = prompt
    }

    override fun resetPrompt(builder: PromptBuilder.() -> Unit) {
        prompt = Prompt.build(prompt = initialPrompt(), init = builder)
    }

    override fun appendToPrompt(builder: PromptBuilder.() -> Unit) {
        promptRef.update { Prompt.build(prompt = it, init = builder) }
    }

    override suspend fun <R> withMcpSession(block: suspend (McpHostSession) -> R): R =
        block(getOrCreateSession())

    private val mcpSessionRef = AtomicReference<McpHostSession?>(null)

    private fun getOrCreateSession(): McpHostSession = mcpSessionRef.updateAndFetch { previous ->
        previous ?: mcpSessionFactory?.invoke(coroutineContext)
    } ?: error("MCP session not available")

    val isPaused: Boolean
        get() = TODO("Not yet implemented")

    suspend fun pause() {
        TODO("Not yet implemented")
    }

    suspend fun resume() {
        TODO("Not yet implemented")
    }

    suspend fun close() {
        mcpSessionRef.exchange(null)?.close()
    }
}
