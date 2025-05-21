package io.github.ptitjes.konvo.tool.web.utils

import com.xemantic.ai.tool.schema.*
import com.xemantic.ai.tool.schema.generator.*
import io.modelcontextprotocol.kotlin.sdk.*
import kotlinx.serialization.json.*

inline fun <reified T> jsonToolInputOf(): Tool.Input = jsonSchemaOf<T>().toToolInput()

fun JsonSchema.toToolInput(): Tool.Input {
    val schemaObject = Json.encodeToJsonElement(this).jsonObject
    return Tool.Input(
        properties = schemaObject["properties"]?.jsonObject ?: JsonObject(emptyMap()),
        required = schemaObject["required"]?.jsonArray?.map { it.jsonPrimitive.content },
    )
}
