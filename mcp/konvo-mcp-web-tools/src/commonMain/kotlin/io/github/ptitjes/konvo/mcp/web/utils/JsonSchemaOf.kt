package io.github.ptitjes.konvo.mcp.web.utils

import io.modelcontextprotocol.kotlin.sdk.types.*
import kotlinx.schema.generator.json.serialization.*
import kotlinx.schema.json.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

// TODO remove when kotlinx-schema-generator-json provides this convenience function
inline fun <reified T> jsonSchemaOf(): JsonSchema {
    val generator = SerializationClassJsonSchemaGenerator.Default
    return generator.generateSchema(serializer<T>().descriptor)
}

inline fun <reified T> jsonToolSchemaOf(): ToolSchema = jsonSchemaOf<T>().toToolSchema()

fun JsonSchema.toToolSchema(): ToolSchema {
    val schemaObject = encodeToJsonObject()
    return ToolSchema(
        properties = schemaObject["properties"]?.jsonObject,
        required = schemaObject["required"]?.jsonArray?.map { it.jsonPrimitive.content },
        defs = schemaObject[$$"$defs"]?.jsonObject,
    )
}
