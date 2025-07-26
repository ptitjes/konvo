package io.github.ptitjes.konvo.core.ai.koog.python

internal data class PythonToken(
    val type: Type,
    val range: IntRange?,
) {
    enum class Type {
        IDENTIFIER,
        STRING_LITERAL,
        INTEGER_LITERAL,
        LBRACKET,
        RBRACKET,
        LPAREN,
        RPAREN,
        COMMA,
        EQUAL,
        EOF,
    }
}

/**
 * A simple regex-based lexer for the subset of Python call expressions with literal arguments.
 */
internal class PythonLexer(private val input: String) {
    fun tokens(): Sequence<PythonToken> = sequence {
        do {
            val token = nextToken()
            yield(token)
        } while (token.type != PythonToken.Type.EOF)
    }

    private var index = 0

    private fun nextToken(): PythonToken {
        maybeSkipWhitespace()

        if (index == input.length) return PythonToken(PythonToken.Type.EOF, null)

        maybeRecognizeSimpleToken()?.let { return it }
        maybeRecognize(identifiers, PythonToken.Type.IDENTIFIER)?.let { return it }
        maybeRecognize(stringLiterals, PythonToken.Type.STRING_LITERAL)?.let { return it }
        maybeRecognize(integerLiterals, PythonToken.Type.INTEGER_LITERAL)?.let { return it }

        unexpectedCharacter()
    }

    private fun maybeSkipWhitespace() {
        while (index < input.length && input[index].isWhitespace()) index++
    }

    private fun maybeRecognizeSimpleToken(): PythonToken? {
        return simpleTokenMap[input[index]]?.let { PythonToken(it, index..<++index) }
    }

    private fun maybeRecognize(regex: Regex, type: PythonToken.Type): PythonToken? =
        regex.matchAt(input, index)?.let {
            index += it.value.length
            PythonToken(type, it.range)
        }

    private fun unexpectedCharacter(): Nothing = error("Unexpected character '${input[index]}' at column ${index + 1}")

    companion object {
        private val simpleTokenMap = mapOf(
            '(' to PythonToken.Type.LPAREN,
            ')' to PythonToken.Type.RPAREN,
            '[' to PythonToken.Type.LBRACKET,
            ']' to PythonToken.Type.RBRACKET,
            ',' to PythonToken.Type.COMMA,
            '=' to PythonToken.Type.EQUAL,
        )

        internal val identifiers = buildRegex {
            val underscore = character('_')
            val identifierStart = disjunction(range('a', 'z'), range('A', 'Z'), underscore)
            val identifierContinue = disjunction(identifierStart, range('0', '9'))

            sequence(identifierStart, zeroOrMore(identifierContinue))
        }

        internal val stringLiterals = buildRegex {
            val singleQuote = character('\'')
            val doubleQuote = character('"')

            val shortStringItemSingleQuote = RegexPart("[^\\\n']")
            val shortStringItemDoubleQuote = RegexPart("[^\\\n\"]")
            val shortString = disjunction(
                sequence(singleQuote, zeroOrMore(shortStringItemSingleQuote), singleQuote),
                sequence(doubleQuote, zeroOrMore(shortStringItemDoubleQuote), doubleQuote),
            )

            val longStringItemSingleQuote = RegexPart("[^\\\n']")
            val longStringItemDoubleQuote = RegexPart("[^\\\n']")
            val longString = disjunction(
                sequence(
                    singleQuote,
                    singleQuote,
                    singleQuote,
                    zeroOrMore(longStringItemSingleQuote),
                    singleQuote,
                    singleQuote,
                    singleQuote
                ),
                sequence(
                    doubleQuote,
                    doubleQuote,
                    doubleQuote,
                    zeroOrMore(longStringItemDoubleQuote),
                    doubleQuote,
                    doubleQuote,
                    doubleQuote
                ),
            )

            disjunction(shortString, longString)
        }

        internal val integerLiterals = buildRegex {
            val zero = character('0')
            val underscore = character('_')
            val maybeUnderscore = zeroOrOne(underscore)

            val nonZeroDigit = range('1', '9')
            val digit = range('0', '9')

            val decInteger = disjunction(
                sequence(nonZeroDigit, zeroOrMore(sequence(maybeUnderscore, digit))),
                sequence(oneOrMore(zero), zeroOrMore(sequence(maybeUnderscore, zero))),
            )

            val binDigit = characters('0', '1')
            val binPrefix = characters('b', 'B')
            val binInteger = sequence(zero, binPrefix, oneOrMore(sequence(maybeUnderscore, binDigit)))

            val octDigit = range('0', '7')
            val octPrefix = characters('o', 'O')
            val octInteger = sequence(zero, octPrefix, oneOrMore(sequence(maybeUnderscore, octDigit)))

            val hexDigit = disjunction(digit, range('a', 'f'), range('A', 'F'))
            val hexPrefix = characters('x', 'X')
            val hexInteger = sequence(zero, hexPrefix, oneOrMore(sequence(maybeUnderscore, hexDigit)))

            disjunction(decInteger, binInteger, octInteger, hexInteger)
        }
    }
}

private data class RegexPart(val pattern: String)

private class RegexBuilder {
    fun character(char: Char) = RegexPart(escapeChar(char))
    fun characters(vararg chars: Char) = RegexPart(chars.joinToString("|", "(?:", ")") { escapeChar(it) })
    fun range(from: Char, to: Char) = RegexPart("[${escapeChar(from)}-${escapeChar(to)}]")

    fun sequence(vararg parts: RegexPart) = RegexPart(parts.joinToString("") { it.pattern })

    fun disjunction(vararg parts: RegexPart) = RegexPart(parts.joinToString("|", "(?:", ")") { it.pattern })

    fun zeroOrOne(part: RegexPart) = RegexPart("(?:${part.pattern})?")
    fun zeroOrMore(part: RegexPart) = RegexPart("(?:${part.pattern})*")
    fun oneOrMore(part: RegexPart) = RegexPart("(?:${part.pattern})+")

    private fun escapeChar(char: Char): String = "$char"
}

private fun buildRegex(action: RegexBuilder.() -> RegexPart): Regex {
    return Regex(RegexBuilder().action().pattern)
}
