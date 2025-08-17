package io.github.ptitjes.konvo.core.agents.toolkit.python

import io.github.ptitjes.konvo.core.agents.toolkit.python.PythonNode.*
import io.github.ptitjes.konvo.core.agents.toolkit.python.PythonNode.Literal.*
import kotlin.test.*

class PythonParserTests {
    @Test
    fun `parses multiple calls`() {
        testParse(
            input = """[web_fetch(url="https://en.wikipedia.org/wiki/Angelina_Jolie_filmography"), web_fetch(url="https://www.imdb.com/list/ls005528877/"), web_fetch(url="https://www.imdb.com/title/tt0944835/")]""",
            expected = listOf(
                CallExpr(
                    name = "web_fetch", arguments = listOf(
                        Argument(
                            name = "url",
                            value = StringLiteral(value = "https://en.wikipedia.org/wiki/Angelina_Jolie_filmography")
                        )
                    )
                ),
                CallExpr(
                    name = "web_fetch",
                    arguments = listOf(
                        Argument(
                            name = "url",
                            value = StringLiteral(value = "https://www.imdb.com/list/ls005528877/")
                        )
                    )
                ),
                CallExpr(
                    name = "web_fetch",
                    arguments = listOf(
                        Argument(
                            name = "url",
                            value = StringLiteral(value = "https://www.imdb.com/title/tt0944835/")
                        )
                    )
                ),
            ),
        ) { parseCalls() }
    }

    @Test
    fun `parses single call`() {
        testParse(
            input = """wikipedia_search(query="test")""",
            expected = CallExpr(
                name = "wikipedia_search",
                arguments = listOf(
                    Argument(
                        name = "query",
                        value = StringLiteral("test"),
                    )
                ),
            ),
        ) { parseCall() }
    }

    @Test
    fun `parses literals`() {
        testParse(""""bim"""", StringLiteral("bim")) { parseLiteral() }
        testParse("""1""", IntegerLiteral(1)) { parseLiteral() }
        testParse("""True""", BooleanLiteral(true)) { parseLiteral() }
        testParse("""None""", NullLiteral) { parseLiteral() }
    }

    private fun <T> testParse(
        input: String,
        expected: T,
        parseAction: PythonParser.() -> T,
    ) {
        val lexer = PythonLexer(input)
        val parser = PythonParser(input, lexer.tokens())
        val node = parser.parseAction()
        assertEquals(expected, node)
    }
}
