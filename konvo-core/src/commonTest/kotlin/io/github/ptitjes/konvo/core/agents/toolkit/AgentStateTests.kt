package io.github.ptitjes.konvo.core.agents.toolkit

import ai.koog.prompt.dsl.Prompt
import io.github.ptitjes.konvo.core.agents.createAgentStateKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

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
}
