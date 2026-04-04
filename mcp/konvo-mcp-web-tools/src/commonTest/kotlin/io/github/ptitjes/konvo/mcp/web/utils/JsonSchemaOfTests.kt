package io.github.ptitjes.konvo.mcp.web.utils

import kotlinx.schema.generator.json.*
import kotlinx.schema.json.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.test.*

@Serializable
@SerialDescription("Some foo class")
data class Foo(
    @SerialDescription("Some bar property")
    val bar: String,
)

class JsonSchemaOfTests {
    private val prettyJson = Json { prettyPrint = true }

    @Test
    fun `jsonSchemaOf should generate schema with description from annotation`() {
        val schema = jsonSchemaOf<Foo>()
        val schemaObject = schema.encodeToJsonObject()

        assertEquals(
            expected = $$"""
                {
                    "$schema": "https://json-schema.org/draft/2020-12/schema",
                    "$id": "io.github.ptitjes.konvo.mcp.web.utils.Foo",
                    "description": "Some foo class",
                    "type": "object",
                    "properties": {
                        "bar": {
                            "type": "string",
                            "description": "Some bar property"
                        }
                    },
                    "additionalProperties": false,
                    "required": [
                        "bar"
                    ]
                }
            """.trimIndent(),
            actual = prettyJson.encodeToString(schemaObject),
        )
    }
}
