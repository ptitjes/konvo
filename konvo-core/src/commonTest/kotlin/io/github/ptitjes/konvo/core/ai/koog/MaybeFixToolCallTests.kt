package io.github.ptitjes.konvo.core.ai.koog

import ai.koog.prompt.dsl.*
import ai.koog.prompt.message.*
import kotlinx.datetime.*
import kotlinx.serialization.json.*
import kotlin.test.*

class MaybeFixToolCallTests {

    @Test
    fun testUnquotedName() {
        testFixToolCalls("""{"name":wikipedia_search,"parameters":{"query":"test"}}""") {
            call("wikipedia_search", mapOf("query" to JsonPrimitive("test")))
        }
    }

    @Test
    fun testUnwrappedParameters() {
        testFixToolCalls("""{"name":"wikipedia_search","query":"test"}""") {
            call("wikipedia_search", mapOf("query" to JsonPrimitive("test")))
        }
    }

    @Test
    fun testBothUnquotedNameAndUnwrappedParameters() {
        testFixToolCalls("""{"name":wikipedia_search,"query":"test"}""") {
            call("wikipedia_search", mapOf("query" to JsonPrimitive("test")))
        }
    }

    @Test
    fun testPythonFormattedToolCall() {
        testFixToolCalls("""[wikipedia_search(query="test")]""") {
            call("wikipedia_search", mapOf("query" to JsonPrimitive("test")))
        }
    }

    @Test
    fun testMultiplePythonFormattedToolCalls() {
        testFixToolCalls("""[web_fetch(url="https://en.wikipedia.org/wiki/Angelina_Jolie_filmography"), web_fetch(url="https://www.imdb.com/list/ls005528877/"), web_fetch(url="https://www.imdb.com/title/tt0944835/")]""") {
            call("web_fetch", mapOf("url" to JsonPrimitive("https://en.wikipedia.org/wiki/Angelina_Jolie_filmography")))
            call("web_fetch", mapOf("url" to JsonPrimitive("https://www.imdb.com/list/ls005528877/")))
            call("web_fetch", mapOf("url" to JsonPrimitive("https://www.imdb.com/title/tt0944835/")))
        }
    }

    fun testFixToolCalls(
        text: String,
        expectation: PromptBuilder.ToolMessageBuilder.() -> Unit,
    ) {
        val expectedCalls = prompt("test") { tool { expectation() } }.messages
        expectedCalls.forEach { require(it is Message.Tool.Call) }

        @Suppress("UNCHECKED_CAST")
        expectedCalls as List<Message.Tool.Call>

        val sourceMessage = Message.Assistant(
            content = text,
            metaInfo = ResponseMetaInfo(
                timestamp = Clock.System.now(),
            ),
        )

        val fixedMessages = sourceMessage.maybeFixToolCalls()
        assertEquals(expectedCalls.size, fixedMessages.size)

        expectedCalls.zip(fixedMessages).forEach { (expected, actual) ->
            assertIs<Message.Tool.Call>(actual)
            assertEquals(expected.tool, actual.tool)
            assertEquals(expected.contentJson, actual.contentJson)
        }
    }
}

private fun PromptBuilder.ToolMessageBuilder.call(tool: String, arguments: Map<String, JsonElement>) {
    call(null, tool, Json.encodeToString(arguments))
}
