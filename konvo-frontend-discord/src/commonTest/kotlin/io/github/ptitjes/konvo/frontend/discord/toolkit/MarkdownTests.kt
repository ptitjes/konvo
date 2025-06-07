package io.github.ptitjes.konvo.frontend.discord.toolkit

import ai.koog.prompt.markdown.*
import io.github.ptitjes.konvo.core.conversation.*
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.*

class MarkdownTests {
    @Test
    fun testSubscript() {
        val event = AssistantEvent.ToolUseResult(
            tool = "that_tool",
            arguments = mapOf("url" to JsonPrimitive("https://www.example.com/blablabla")),
            result = ToolCallResult.Success("Bim bam boom")
        )

        val string = markdown {
            subscript { text("Agent called tool"); space(); bold(event.tool) }
            if (event.arguments.isNotEmpty()) blockquote {
                event.arguments.forEach { (name, value) ->
                    subscript { bold(name); space(); text(value.toString()) }
                }
            }
        }

        assertEquals("""
            -# Agent called tool **that_tool**
            > -# **url** "https://www.example.com/blablabla"
        """.trimIndent(), string)
    }
}
