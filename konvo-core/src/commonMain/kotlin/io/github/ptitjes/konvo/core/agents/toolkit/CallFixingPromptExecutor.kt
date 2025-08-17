package io.github.ptitjes.konvo.core.agents.toolkit

import ai.koog.agents.core.tools.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.model.*
import ai.koog.prompt.llm.*
import ai.koog.prompt.message.*
import io.github.ptitjes.konvo.core.agents.toolkit.python.*
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

    override suspend fun moderate(prompt: Prompt, model: LLModel): ModerationResult =
        delegate.moderate(prompt, model)
}

/**
 * This is a dirty hack to account for small models having difficulties producing properly formatted tool calls.
 */
private fun List<Message.Response>.maybeFixToolCalls(): List<Message.Response> = flatMap { it.maybeFixToolCalls() }

/**
 * This is a dirty hack to account for small models having difficulties producing properly formatted tool calls.
 */
internal fun Message.Response.maybeFixToolCalls(): List<Message.Response> {
    return maybeParsePythonToolCalls()
        ?: maybeParseLenientToolCall()
        ?: listOf(this)
}

private fun Message.Response.maybeParseLenientToolCall(): List<Message.Tool.Call>? = runCatching {
    val jsonObject = lenientJson.decodeFromString<JsonObject>(content)

    val toolName = jsonObject["name"]?.jsonPrimitive?.content ?: return null
    val arguments = jsonObject["parameters"]?.jsonObject
        ?: jsonObject.filter { it.key != "name" }

    val toolCallContent = Json.encodeToString<Map<String, JsonElement>>(arguments)

    listOf(
        Message.Tool.Call(
            id = generateToolCallId(toolName, toolCallContent),
            tool = toolName,
            content = toolCallContent,
            metaInfo = metaInfo
        ),
    )
}.getOrNull()

private val lenientJson = Json { isLenient = true }

private fun Message.Response.maybeParsePythonToolCalls(): List<Message.Tool.Call>? = runCatching {
    val lexer = PythonLexer(content)
    val parser = PythonParser(content, lexer.tokens())
    val calls = parser.parseCalls()

    calls.mapIndexed { index, callExpr ->
        val toolName = callExpr.name

        val jsonContent = buildJsonObject {
            callExpr.arguments.forEach { argument ->
                put(
                    key = argument.name,
                    element = when (argument.value) {
                        is PythonNode.Literal.StringLiteral -> JsonPrimitive(argument.value.value)
                        is PythonNode.Literal.IntegerLiteral -> JsonPrimitive(argument.value.value)
                        is PythonNode.Literal.BooleanLiteral -> JsonPrimitive(argument.value.value)
                        is PythonNode.Literal.NullLiteral -> JsonNull
                    },
                )
            }
        }

        val content = Json.encodeToString<JsonObject>(jsonContent)

        Message.Tool.Call(
            id = generateToolCallId(toolName, content, index),
            tool = toolName,
            content = content,
            metaInfo = metaInfo,
        )
    }
}.getOrNull()

private fun generateToolCallId(toolName: String, content: String, index: Int = 0): String {
    // Create a deterministic ID using tool name, content hash, and index
    val combined = "$toolName:$content:$index"
    val hashCode = combined.hashCode()

    // Format as "fixed_tool_call_" + positive hash to match common ID patterns
    return "fixed_tool_call_${hashCode.toUInt()}"
}
