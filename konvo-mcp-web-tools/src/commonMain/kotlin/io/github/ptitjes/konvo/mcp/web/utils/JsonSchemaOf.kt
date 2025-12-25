package io.github.ptitjes.konvo.mcp.web.utils

import com.xemantic.ai.tool.schema.*
import com.xemantic.ai.tool.schema.generator.*
import io.modelcontextprotocol.kotlin.sdk.types.*
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.*

inline fun <reified T> jsonToolInputOf(): ToolSchema = jsonSchemaOf<T>().toToolInput()
inline fun <reified T> jsonToolOutputOf(): ToolSchema = jsonSchemaOf<T>().toToolOutput()

fun JsonSchema.toToolInput(): ToolSchema {
    val schemaObject = Json.encodeToJsonElement(this).jsonObject
    return ToolSchema(
        properties = schemaObject["properties"]?.jsonObject ?: JsonObject(emptyMap()),
        required = schemaObject["required"]?.jsonArray?.map { it.jsonPrimitive.content },
    )
}

fun JsonSchema.toToolOutput(): ToolSchema {
    val schemaObject = Json.encodeToJsonElement(this).jsonObject
    return ToolSchema(
        properties = schemaObject["properties"]?.jsonObject ?: JsonObject(emptyMap()),
        required = schemaObject["required"]?.jsonArray?.map { it.jsonPrimitive.content },
    )
}
