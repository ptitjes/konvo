package io.github.ptitjes.konvo.backend.ollama

import io.github.ptitjes.konvo.core.ai.spi.*
import kotlinx.serialization.json.*

/**
 * This is a dirty hack to account for small models having difficulties producing properly formatted tool calls.
 */
internal fun ChatMessage.Assistant.maybeFixToolCalls(tools: List<Tool>?): ChatMessage.Assistant {
    if (tools.isNullOrEmpty()) return this
    if (toolCalls != null) return this

    val text = text.trim()

    if (text.startsWith("[") && text.endsWith("]")) return parsePythonToolCall(tools)
    if (!text.startsWith("{\"name\":") || !text.endsWith("}")) return this

    val functionNameAndParameters = text.removePrefix("{\"name\":").removeSuffix("}").trim()
    val commaIndex = functionNameAndParameters.indexOf(",")
    if (commaIndex == -1) return this

    val functionName = functionNameAndParameters.substring(0, commaIndex).trim()
        .removePrefix("\"").removeSuffix("\"")
    if (tools.firstOrNull { it.name == functionName } == null) return this

    val parametersProperty = functionNameAndParameters.substring(commaIndex + 1).trim()

    validToolCallOrNull("""{"name":"$functionName",$parametersProperty}""")?.let { return it }
    validToolCallOrNull("""{"name":"$functionName","parameters":{$parametersProperty}}""")?.let { return it }
    return this
}

internal fun ChatMessage.Assistant.parsePythonToolCall(tools: List<Tool>): ChatMessage.Assistant {
    val text = text.trim()

    val functionNameAndParameters = text.removePrefix("[").removeSuffix("]").trim()
    val parenthesisIndex = functionNameAndParameters.indexOf("(")
    if (parenthesisIndex == -1) return this

    val functionName = functionNameAndParameters.substring(0, parenthesisIndex).trim()
    if (tools.firstOrNull { it.name == functionName } == null) return this


    val parametersString = functionNameAndParameters.substring(parenthesisIndex + 1).trim()
        .removePrefix("(").removeSuffix(")").trim()

    val parameters = parametersString.split(",").mapNotNull { parameterString ->
        val splitParameter = parameterString.trim().split('=')
        if (splitParameter.size != 2) return@mapNotNull null
        splitParameter[0] to splitParameter[1]
    }

    val parametersObject = """{${parameters.joinToString(",") { (name, value) -> """"$name":$value""" }}}"""
    validToolCallOrNull("""{"name":"$functionName","parameters":$parametersObject}""")?.let { return it }
    return this
}

private fun validToolCallOrNull(text: String): ChatMessage.Assistant? {
    try {
        val jsonObject = Json.Default.decodeFromString<JsonObject>(text)
        val name = jsonObject["name"]?.jsonPrimitive?.content ?: return null
        val arguments = jsonObject["parameters"]?.jsonObject ?: return null
        return ChatMessage.Assistant(
            text = "",
            toolCalls = listOf(ToolCall(name, arguments)),
        )
    } catch (_: Throwable) {
        return null
    }
}
