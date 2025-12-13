package io.github.ptitjes.konvo.mcp.web.utils

import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.*
import kotlinx.serialization.json.*

inline fun <reified I> Server.addStringTool(
    name: String,
    description: String,
    crossinline handler: suspend (I) -> String,
) {
    addTool(
        name = name,
        description = description,
        inputSchema = jsonToolInputOf<I>(),
    ) { request ->
        val decodedRequest = Json.decodeFromJsonElement<I>(request.arguments ?: JsonNull)
        val result = handler(decodedRequest)
        CallToolResult(content = listOf(TextContent(result)))
    }
}

inline fun <reified I, reified O> Server.addJsonTool(
    name: String,
    description: String,
    crossinline handler: suspend (I) -> O,
) {
    addTool(
        name = name,
        description = description,
        inputSchema = jsonToolInputOf<I>(),
        outputSchema = jsonToolOutputOf<O>(),
    ) { request ->
        val decodedRequest = Json.decodeFromJsonElement<I>(request.arguments)
        val decodedResult = handler(decodedRequest)
        val encodedResult = Json.encodeToJsonElement(decodedResult)
        CallToolResult(
            content = listOf(TextContent(encodedResult.toString())),
            structuredContent = encodedResult.jsonObject,
        )
    }
}
