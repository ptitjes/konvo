package io.github.ptitjes.konvo.core.agents.toolkit.python

import kotlin.test.*

class PythonLexerTests {
    @Test
    fun `lexer successes`() {
        lexerSuccessTest(
            """wikipedia_search(query="test")""",
            listOf(
                PythonToken(PythonToken.Type.IDENTIFIER, 0..15),
                PythonToken(PythonToken.Type.LPAREN, 16..16),
                PythonToken(PythonToken.Type.IDENTIFIER, 17..21),
                PythonToken(PythonToken.Type.EQUAL, 22..22),
                PythonToken(PythonToken.Type.STRING_LITERAL, 23..28),
                PythonToken(PythonToken.Type.RPAREN, 29..29),
                PythonToken(PythonToken.Type.EOF, null),
            ),
        )
    }

    @Test
    fun `identifiers regex`() {
        testRegex(
            regex = PythonLexer.identifiers,
            valid = listOf("some_identifier", "SomeIdentifier", "Some01Identifier2", "_foo", "__bar"),
            invalid = listOf("1abc"),
        )
    }

    @Test
    fun `string literals regex`() {
        testRegex(
            regex = PythonLexer.stringLiterals,
            valid = listOf(
                """"foo"""",
            ),
        )
    }

    @Test
    fun `integer literals regex`() {
        testRegex(
            regex = PythonLexer.integerLiterals,
            valid = listOf(
                "2147483647", "79228162514264337593543950336", "100_000_000_000",
                "0", "00000", "000_0",
                "0b100110111", "0b_1110_0101",
                "0o177", "0o377",
                "0x_0", "0xdeadbeef",
            ),
            invalid = listOf(
                "0_", "0x", "0xdeadbeef_",
            )
        )
    }

    private fun lexerSuccessTest(input: String, expected: List<PythonToken>) {
        assertEquals(expected, PythonLexer(input).tokens().toList())
    }

    private fun testRegex(
        regex: Regex,
        valid: List<String>,
        invalid: List<String>? = null,
    ) {
        valid.forEach {
            assertTrue(regex.matches(it), "Regex should match '$it'")
        }
        invalid?.forEach {
            assertFalse(regex.matches(it), "Regex should not match '$it'")
        }
    }
}
