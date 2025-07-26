package io.github.ptitjes.konvo.core.ai.koog.python

internal sealed interface PythonNode {
    data class CallExpr(val name: String, val arguments: List<Argument>) : PythonNode

    data class Argument(val name: String, val value: Literal) : PythonNode

    sealed interface Literal : PythonNode {
        data class StringLiteral(val value: String) : Literal
        data class IntegerLiteral(val value: Int) : Literal
        data class BooleanLiteral(val value: Boolean) : Literal
        data object NullLiteral : Literal
    }
}

/**
 * A simple recursive descent parser for the subset of Python call expressions with literal arguments.
 */
internal class PythonParser(
    private val input: String,
    private val tokens: Sequence<PythonToken>,
) {
    private var token = tokens.first()
    private val tokenValue get() = input.substring(token.range ?: error("End of file"))
    private val tokenType get() = token.type

    private fun nextToken(): PythonToken {
        token = tokens.first()
        return token
    }

    private fun accept(accepted: PythonToken.Type): Boolean {
        if (tokenType == accepted) {
            nextToken()
            return true
        }
        return false
    }

    private fun expect(expected: PythonToken.Type): Boolean {
        if (accept(expected)) return true
        unexpectedSymbol()
    }

    internal fun parseCalls(): List<PythonNode.CallExpr> {
        val calls = mutableListOf<PythonNode.CallExpr>()
        expect(PythonToken.Type.LBRACKET)
        do {
            if (tokenType == PythonToken.Type.RBRACKET) break
            calls += parseCall()
        } while (accept(PythonToken.Type.COMMA))
        expect(PythonToken.Type.RBRACKET)
        return calls
    }

    internal fun parseCall(): PythonNode.CallExpr {
        val name = parseIdentifier()
        val arguments = parseArguments()
        return PythonNode.CallExpr(name, arguments)
    }

    private fun parseIdentifier(): String {
        if (tokenType != PythonToken.Type.IDENTIFIER) unexpectedSymbol()
        val identifier = tokenValue
        nextToken()
        return identifier
    }

    private fun parseArguments(): List<PythonNode.Argument> {
        val arguments = mutableListOf<PythonNode.Argument>()
        expect(PythonToken.Type.LPAREN)
        do {
            if (tokenType == PythonToken.Type.RPAREN) break
            arguments += parseArgument()
        } while (accept(PythonToken.Type.COMMA))
        expect(PythonToken.Type.RPAREN)
        return arguments
    }

    private fun parseArgument(): PythonNode.Argument {
        val name = parseIdentifier()
        expect(PythonToken.Type.EQUAL)
        val value = parseLiteral()
        return PythonNode.Argument(name, value)
    }

    internal fun parseLiteral(): PythonNode.Literal = when (tokenType) {
        PythonToken.Type.IDENTIFIER -> when (tokenValue) {
            "None" -> PythonNode.Literal.NullLiteral
            "True" -> PythonNode.Literal.BooleanLiteral(true)
            "False" -> PythonNode.Literal.BooleanLiteral(false)
            else -> unexpectedSymbol()
        }

        PythonToken.Type.STRING_LITERAL -> PythonNode.Literal.StringLiteral(tokenValue.unquoteStringLiteral())
        PythonToken.Type.INTEGER_LITERAL -> PythonNode.Literal.IntegerLiteral(tokenValue.toInt())
        else -> unexpectedSymbol()
    }.also { nextToken() }

    private fun unexpectedSymbol(): Nothing = error("Unexpected symbol '${tokenValue}' at ${token.range}")
}

private fun String.unquoteStringLiteral(): String {
    return when {
        startsWith("'''") || startsWith("\"\"\"") -> substring(3, length - 3)
        startsWith("'") || startsWith("\"") -> substring(1, length - 1)
        else -> error("Unexpected string literal: $this")
    }
}
