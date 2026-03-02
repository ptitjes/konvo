package io.github.ptitjes.konvo.core.agents.toolkit

import ai.koog.prompt.dsl.*
import io.github.ptitjes.konvo.core.agents.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

class AgentStateTests {

    @Test
    fun `load and update state`() = runTest {
        val context = DefaultAgentContext(
            coroutineContext = coroutineContext,
            initialPrompt = { Prompt.Empty }
        )

        val key = createAgentStateKey<Int>("counter")

        assertNull(context.loadState(key))

        context.updateState(key, 1)
        assertEquals(1, context.loadState(key))

        context.updateState(key) { (it ?: 0) + 1 }
        assertEquals(2, context.loadState(key))
    }

    @Test
    fun `multiple states`() = runTest {
        val context = DefaultAgentContext(
            coroutineContext = coroutineContext,
            initialPrompt = { Prompt.Empty }
        )

        val key1 = createAgentStateKey<Int>("counter")
        val key2 = createAgentStateKey<String>("name")

        context.updateState(key1, 10)
        context.updateState(key2, "Konvo")

        assertEquals(10, context.loadState(key1))
        assertEquals("Konvo", context.loadState(key2))
    }

    @Test
    fun `concurrent updates`() = runTest {
        val context = DefaultAgentContext(
            coroutineContext = coroutineContext,
            initialPrompt = { Prompt.Empty }
        )

        val key = createAgentStateKey<Int>("counter")
        context.updateState(key, 0)

        val n = 100
        withContext(Dispatchers.Default) {
            val jobs = List(n) {
                launch {
                    repeat(100) {
                        context.updateState(key) { (it ?: 0) + 1 }
                    }
                }
            }
            jobs.joinAll()
        }

        assertEquals(n * 100, context.loadState(key))
    }
}
