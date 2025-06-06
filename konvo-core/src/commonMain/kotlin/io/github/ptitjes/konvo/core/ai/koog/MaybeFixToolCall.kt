package io.github.ptitjes.konvo.core.ai.koog

import ai.koog.agents.core.dsl.builder.*
import ai.koog.prompt.message.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

fun AIAgentSubgraphBuilderBase<*, *>.nodeMaybeFixToolCall(name: String? = null) =
    node<Message.Response, Message.Response>(name) { response ->
        val fixedResponse = response.maybeFixToolCall()
        if (response == fixedResponse) return@node response

        llm.writeSession {
            prompt = prompt.copy(
                messages = prompt.messages.map { message -> if (message != response) message else fixedResponse }
            )
        }

        fixedResponse
    }

/**
 * This is a dirty hack to account for small models having difficulties producing properly formatted tool calls.
 */
fun Message.Response.maybeFixToolCall(): Message.Response {
    if (this is Message.Tool.Call) return this

    val text = content.trim()

    if (text.startsWith("[") && text.endsWith("]")) return maybeParsePythonToolCall()
    if (!text.startsWith("{\"name\":") || !text.endsWith("}")) return this

    val functionNameAndParameters = text.removePrefix("{\"name\":").removeSuffix("}").trim()
    val commaIndex = functionNameAndParameters.indexOf(",")
    if (commaIndex == -1) return this

    val functionName = functionNameAndParameters.substring(0, commaIndex).trim()
        .removePrefix("\"").removeSuffix("\"")

    val parametersProperty = functionNameAndParameters.substring(commaIndex + 1).trim()

    validToolCallOrNull("""{"name":"$functionName",$parametersProperty}""")?.let { return it }
    validToolCallOrNull("""{"name":"$functionName","parameters":{$parametersProperty}}""")?.let { return it }
    return this
}

internal fun Message.Response.maybeParsePythonToolCall(): Message.Response {
    val text = content.trim()

    val functionNameAndParameters = text.removePrefix("[").removeSuffix("]").trim()
    val parenthesisIndex = functionNameAndParameters.indexOf("(")
    if (parenthesisIndex == -1) return this

    val functionName = functionNameAndParameters.substring(0, parenthesisIndex).trim()
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

private fun Message.Response.validToolCallOrNull(text: String): Message.Tool.Call? {
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
