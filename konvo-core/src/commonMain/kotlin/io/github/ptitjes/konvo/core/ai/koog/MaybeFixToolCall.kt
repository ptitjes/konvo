package io.github.ptitjes.konvo.core.ai.koog

import ai.koog.agents.core.tools.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.model.*
import ai.koog.prompt.llm.*
import ai.koog.prompt.message.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*

/**
 * This is a dirty hack to account for small models having difficulties producing properly formatted tool calls.
 */
class CallFixingPromptExecutor(private val delegate: PromptExecutor) : PromptExecutor {
    override suspend fun execute(prompt: Prompt, model: LLModel, tools: List<ToolDescriptor>): List<Message.Response> =
        delegate.execute(prompt, model, tools).maybeFixToolCalls()

    override suspend fun executeStreaming(prompt: Prompt, model: LLModel): Flow<String> =
        delegate.executeStreaming(prompt, model)
}

/**
 * This is a dirty hack to account for small models having difficulties producing properly formatted tool calls.
 */
fun List<Message.Response>.maybeFixToolCalls(): List<Message.Response> = flatMap { it.maybeFixToolCalls() }

/**
 * This is a dirty hack to account for small models having difficulties producing properly formatted tool calls.
 */
fun Message.Response.maybeFixToolCalls(): List<Message.Response> {
    val text = content.trim()

    if (text.startsWith("[") && text.endsWith("]")) return maybeParsePythonToolCalls()
    if (!text.startsWith("{\"name\":") || !text.endsWith("}")) return listOf(this)

    val functionNameAndParameters = text.removePrefix("{\"name\":").removeSuffix("}").trim()
    val commaIndex = functionNameAndParameters.indexOf(",")
    if (commaIndex == -1) return listOf(this)

    val functionName = functionNameAndParameters.substring(0, commaIndex).trim()
        .removePrefix("\"").removeSuffix("\"")

    val parametersProperty = functionNameAndParameters.substring(commaIndex + 1).trim()

    validToolCallOrNull("""{"name":"$functionName",$parametersProperty}""")?.let { return listOf(it) }
    validToolCallOrNull("""{"name":"$functionName","parameters":{$parametersProperty}}""")?.let { return listOf(it) }
    return listOf(this)
}

internal fun Message.Response.maybeParsePythonToolCalls(): List<Message.Response> {
    val text = content.trim()
    val maybeToolCalls = parsePythonToolCallsOrNull(text, metaInfo)
    return maybeToolCalls ?: listOf(this)
}

fun parsePythonToolCallsOrNull(input: String, metaInfo: ResponseMetaInfo): List<Message.Tool.Call>? {
    return runCatching { DummyPythonParser(input, metaInfo).parseToolCallBlock() }.getOrNull()
}

private class DummyPythonParser(input: String, private val metaInfo: ResponseMetaInfo) {
    private val lexer = DummyPythonLexer(input)

    fun parseToolCallBlock(): List<Message.Tool.Call> {
        lexer.leftBracket()
        val calls = parseToolCalls()
        lexer.rightBracket()
        return calls
    }

    fun parseToolCalls(): List<Message.Tool.Call> = buildList {
        do {
            lexer.consumeSpaces()
            add(parseToolCall())
            lexer.consumeSpaces()
        } while (lexer.maybeComma())
        lexer.consumeSpaces()
    }


    fun parseToolCall(): Message.Tool.Call {
        val functionName = lexer.identifier()

        lexer.consumeSpaces()
        lexer.leftParen()
        val arguments = parseNamedArguments()
        lexer.rightParen()
        lexer.consumeSpaces()

        val argumentsObject = """{${arguments.joinToString(",") { (name, value) -> """"$name":$value""" }}}"""
        return validToolCallOrNull("""{"name":"$functionName","parameters":$argumentsObject}""", metaInfo)
            ?: error("Failed to parse tool call")
    }

    fun parseNamedArguments(): List<Pair<String, String>> = buildList {
        do {
            lexer.consumeSpaces()
            add(parseNamedArgument())
            lexer.consumeSpaces()
        } while (lexer.maybeComma())
        lexer.consumeSpaces()
    }

    fun parseNamedArgument(): Pair<String, String> {
        val identifier = lexer.identifier()
        lexer.consumeSpaces()
        lexer.equal()
        lexer.consumeSpaces()
        val value = parseLiteral()
        return identifier to value
    }

    fun parseLiteral(): String {
        lexer.maybeBooleanLiteral()?.let { return it.toString() }
        lexer.maybeNumericLiteral()?.let { return it.toString() }
        lexer.maybeStringLiteral()?.let { return it }
        error("Failed to parse tool call argument")
    }
}

private class DummyPythonLexer(val input: String) {
    var currentIndex = 0

    fun leftBracket() = consume('[')
    fun rightBracket() = consume(']')

    fun leftParen() = consume('(')
    fun rightParen() = consume(')')

    fun maybeComma(): Boolean = maybeConsume(',')

    fun quote() = consume('"')
    fun maybeQuote(): Boolean = maybeConsume('"')
    fun maybeBackslash(): Boolean = maybeConsume('\\')

    fun equal() = consume('=')
    fun maybeMinus(): Boolean = maybeConsume('-')

    fun identifier(): String = buildString {
        if (currentChar.isLetterOrUnderscore()) append(consume())
        while (currentChar.isLetterDigitOrUnderscore()) append(consume())
    }

    fun maybeNumericLiteral(): Number? {
        try {
            val negative = maybeMinus()

            val integerPart = buildString {
                while (currentChar.isDigit()) append(consume())
            }

            if (!maybeConsume('.')) return "${"-".takeIf { negative }}$integerPart".toInt()

            val decimalPart = buildString {
                while (currentChar.isDigit()) append(consume())
            }

            return "${"-".takeIf { negative }}$integerPart.$decimalPart".toDouble()
        } catch (_: Exception) {
            return null
        }
    }

    fun maybeStringLiteral(): String? {
        return if (maybeQuote()) buildString {
            append('"')
            do {
                if (maybeBackslash()) append('\\')
                append(consume())
            } while (currentChar != '"')
            quote()
            append('"')
        } else null
    }

    fun maybeBooleanLiteral(): Boolean? = when {
        maybeConsume('0') -> false
        maybeConsume('1') -> true
        maybeConsume("False") -> false
        maybeConsume("True") -> true
        else -> null
    }

    private val currentChar: Char get() = input[currentIndex]

    private fun consume(expectedString: String) {
        expectedString.forEach { consume(it) }
    }

    private fun maybeConsume(expectedString: String): Boolean {
        val charCount = expectedString.length
        for (index in 0 until charCount) {
            if (input[currentIndex + index] != expectedString[index]) return false
        }
        currentIndex += charCount
        return true
    }

    private fun consume(expectedChar: Char) {
        if (currentChar != expectedChar) error("Expected '$expectedChar' but got '$currentChar'")
        currentIndex++
    }

    private fun maybeConsume(expectedChar: Char): Boolean {
        if (currentChar != expectedChar) return false
        currentIndex++
        return true
    }

    private fun consume(): Char {
        val char = currentChar
        currentIndex++
        return char
    }

    fun consumeSpaces() {
        while (input[currentIndex] == ' ' || input[currentIndex] == '\t') currentIndex++
    }

}

private fun Char.isLetterOrUnderscore(): Boolean = isLetter() || this == '_'
private fun Char.isLetterDigitOrUnderscore(): Boolean = isLetterOrDigit() || this == '_'

private fun Message.Response.validToolCallOrNull(text: String): Message.Tool.Call? {
    return validToolCallOrNull(text, metaInfo)
}

private fun validToolCallOrNull(text: String, metaInfo: ResponseMetaInfo): Message.Tool.Call? {
    try {
        val jsonObject = Json.decodeFromString<JsonObject>(text)

        val toolName = jsonObject["name"]?.jsonPrimitive?.content ?: return null
        val arguments = jsonObject["parameters"]?.jsonObject ?: return null

        val toolCallContent = Json.encodeToString(arguments)

        val id = generateToolCallId(toolName, toolCallContent)

        return Message.Tool.Call(id = id, tool = toolName, content = toolCallContent, metaInfo = metaInfo)
    } catch (_: Throwable) {
        return null
    }
}

private fun generateToolCallId(toolName: String, content: String, index: Int = 0): String {
    // Create a deterministic ID using tool name, content hash, and index
    val combined = "$toolName:$content:$index"
    val hashCode = combined.hashCode()

    // Format as "fixed_tool_call_" + positive hash to match common ID patterns
    return "fixed_tool_call_${hashCode.toUInt()}"
}
