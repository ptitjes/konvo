package io.github.ptitjes.konvo.core.ai.koog

import ai.koog.prompt.message.*
import kotlinx.datetime.*
import kotlinx.serialization.json.*
import kotlin.test.*

class MaybeFixToolCallTests {

    @Test
    fun testUnquotedName() {
        testFixToolCall(
            """{"name":wikipedia_search,"parameters":{"query":"test"}}""",
            "wikipedia_search",
            mapOf("query" to JsonPrimitive("test")),
        )
    }

    @Test
    fun testUnwrappedParameters() {
        testFixToolCall(
            """{"name":"wikipedia_search","query":"test"}""",
            "wikipedia_search",
            mapOf("query" to JsonPrimitive("test")),
        )
    }

    @Test
    fun testBothUnquotedNameAndUnwrappedParameters() {
        testFixToolCall(
            """{"name":wikipedia_search,"query":"test"}""",
            "wikipedia_search",
            mapOf("query" to JsonPrimitive("test")),
        )
    }

    @Test
    fun testPythonFormattedToolCall() {
        testFixToolCall(
            """[wikipedia_search(query="test")]""",
            "wikipedia_search",
            mapOf("query" to JsonPrimitive("test")),
        )
    }

    fun testFixToolCall(
        text: String,
        expectedToolName: String,
        expectedToolArguments: Map<String, JsonElement>,
    ) {
        val sourceMessage = Message.Assistant(
            content = text,
            metaInfo = ResponseMetaInfo(
                timestamp = Clock.System.now(),
            ),
        )

        val fixedMessage = sourceMessage.maybeFixToolCall()
        assertIs<Message.Tool.Call>(fixedMessage)
        assertEquals(expectedToolName, fixedMessage.tool)
        assertEquals(expectedToolArguments, fixedMessage.contentJson)
    }
}
