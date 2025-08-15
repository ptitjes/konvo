package io.github.ptitjes.konvo.frontend.compose.components.json

import com.xemantic.ai.tool.schema.ArraySchema
import com.xemantic.ai.tool.schema.BooleanSchema
import com.xemantic.ai.tool.schema.IntegerSchema
import com.xemantic.ai.tool.schema.JsonSchema
import com.xemantic.ai.tool.schema.NumberSchema
import com.xemantic.ai.tool.schema.ObjectSchema
import com.xemantic.ai.tool.schema.StringSchema
import com.xemantic.ai.tool.schema.WithDefinitions
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.collections.plus

fun defaultJsonElement(schema: JsonSchema, rootSchema: WithDefinitions): JsonElement = when (schema) {
    is JsonSchema.Ref -> defaultJsonElement(rootSchema.resolveRef(schema), rootSchema)
    is ObjectSchema -> defaultJsonObject(schema, rootSchema)
    is ArraySchema -> defaultJsonArray()
    is BooleanSchema -> JsonPrimitive(false)
    is IntegerSchema -> JsonPrimitive(0)
    is NumberSchema -> JsonPrimitive(0.0)
    is StringSchema -> JsonPrimitive("")
    is JsonSchema.Const -> JsonPrimitive(schema.const)
}

fun defaultJsonArray(): JsonArray = JsonArray(emptyList())

fun defaultJsonObject(schema: ObjectSchema, rootSchema: WithDefinitions): JsonObject {
    val oneOf = schema.oneOf
    val allOf = schema.allOf

    return when {
        oneOf != null && oneOf.isNotEmpty() -> defaultJsonObject(oneOf.first() as ObjectSchema, rootSchema)
        allOf != null && allOf.isNotEmpty() -> defaultJsonObject(
            properties = allOf.fold(emptyMap()) { properties, schema ->
                properties + ((rootSchema.maybeResolveRef(schema) as ObjectSchema).properties ?: emptyMap())
            },
            rootSchema = rootSchema,
        )

        else -> {
            defaultJsonObject(schema.properties, rootSchema)
        }
    }
}

private fun defaultJsonObject(properties: Map<String, JsonSchema>?, rootSchema: WithDefinitions): JsonObject {
    return JsonObject(
        properties?.mapValues { (_, propSchema) ->
            defaultJsonElement(propSchema, rootSchema)
        } ?: emptyMap()
    )
}
