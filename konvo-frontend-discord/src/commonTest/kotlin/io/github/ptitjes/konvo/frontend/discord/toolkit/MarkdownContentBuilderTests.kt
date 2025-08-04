package io.github.ptitjes.konvo.frontend.discord.toolkit

import ai.koog.prompt.markdown.*
import kotlin.test.*

class MarkdownContentBuilderTests {
    @Test
    fun `subscript extension uses correct Discord-specific markdown`() {
        val string = markdown {
            subscript { text("Agent called tool"); space(); bold("that_tool") }
            blockquote {
                subscript { bold("url"); space(); text("\"https://www.example.com/blablabla\"") }
            }
        }

        assertEquals("""
            -# Agent called tool **that_tool**
            > -# **url** "https://www.example.com/blablabla"
        """.trimIndent(), string)
    }
}
