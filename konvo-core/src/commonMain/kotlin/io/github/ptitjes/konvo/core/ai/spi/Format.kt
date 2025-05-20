package io.github.ptitjes.konvo.core.ai.spi

import com.xemantic.ai.tool.schema.JsonSchema
import com.xemantic.ai.tool.schema.generator.jsonSchemaOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

sealed interface Format {
    @JvmInline
    value class FormatType(val value: String) : Format {
        companion object {
            val JSON: FormatType = FormatType("json")
        }
    }

    @JvmInline
    value class FormatSchema(val schema: JsonObject) : Format
}

inline fun <reified T> jsonSchemaOf(): JsonObject {
    return jsonSchemaOf<T>().toJsonObject()
}

fun JsonSchema.toJsonObject(): JsonObject {
    return Json.encodeToJsonElement(this).jsonObject
}
