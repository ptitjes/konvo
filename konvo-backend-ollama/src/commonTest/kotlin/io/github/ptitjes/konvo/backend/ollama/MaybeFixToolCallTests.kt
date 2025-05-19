package io.github.ptitjes.konvo.backend.ollama

import io.github.ptitjes.konvo.core.ai.spi.*
import kotlinx.serialization.json.*
import kotlin.test.*

class MaybeFixToolCallTests {
    val fakeTools = listOf(
        makeFakeTool("wikipedia_search", mapOf("query" to "string", "limit" to "number")),
        makeFakeTool("wikipedia_get_page", mapOf("key" to "string")),
    )

    @Test
    fun testUnquotedName() {
        testFixToolCall(
            """{"name":wikipedia_search,"parameters":{"query":"test"}}""",
            ToolCall(
                name = "wikipedia_search",
                arguments = mapOf("query" to JsonPrimitive("test")),
            ),
        )
    }

    @Test
    fun testUnwrappedParameters() {
        testFixToolCall(
            """{"name":"wikipedia_search","query":"test"}""",
            ToolCall(
                name = "wikipedia_search",
                arguments = mapOf("query" to JsonPrimitive("test")),
            ),
        )
    }

    @Test
    fun testBothUnquotedNameAndUnwrappedParameters() {
        testFixToolCall(
            """{"name":wikipedia_search,"query":"test"}""",
            ToolCall(
                name = "wikipedia_search",
                arguments = mapOf("query" to JsonPrimitive("test")),
            ),
        )
    }

    @Test
    fun testPythonFormattedToolCall() {
        testFixToolCall(
            """[wikipedia_search(query="test")]""",
            ToolCall(
                name = "wikipedia_search",
                arguments = mapOf("query" to JsonPrimitive("test")),
            ),
        )
    }

    fun testFixToolCall(text: String, expectedToolCall: ToolCall) {
        val sourceMessage = ChatMessage.Assistant(
            text = text,
        )

        val fixedMessage = sourceMessage.maybeFixToolCalls(fakeTools)
        assertTrue(fixedMessage.text == "")

        val fixedToolCalls = fixedMessage.toolCalls
        assertNotNull(fixedToolCalls)
        assertTrue(fixedToolCalls.size == 1)
        assertEquals(expectedToolCall, fixedToolCalls[0])
    }
}

fun makeFakeTool(
    name: String,
    propertiesType: Map<String, String>,
): Tool {
    return Tool(
        name = name,
        description = "",
        parameters = ToolParameters(
            properties = propertiesType.mapValues { (_, type) ->
                buildJsonObject { put("type", type) }
            },
            required = listOf()
        ),
        askPermission = false,
        evaluator = { arguments -> "" },
    )
}
