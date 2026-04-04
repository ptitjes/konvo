package io.github.ptitjes.konvo.plugin.core.tools

import ai.koog.agents.core.tools.*
import de.infix.testBalloon.framework.core.*
import io.modelcontextprotocol.kotlin.sdk.types.*
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import kotlinx.serialization.json.*
import kotlin.test.*

val CustomMcpToolDefinitionParserTests by testSuite {

    test("should parse no-arg tool definition") {
        val tool = Tool(
            name = "Some tool",
            description = "Some tool description",
            inputSchema = ToolSchema(),
        )

        val descriptor = CustomMcpToolDescriptorParser.parse(tool)

        assertEquals(
            ToolDescriptor(
                name = "Some tool",
                description = "Some tool description",
                requiredParameters = listOf(),
                optionalParameters = listOf(),
            ),
            descriptor,
        )
    }

    test("should parse nullable primitive parameter") {
        val tool = Tool(
            name = "Some tool",
            description = "Some tool description",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("nullableStringParam") {
                        putJsonArray("type") {
                            add("string")
                            add("null")
                        }
                        put("description", "Some nullable string parameter")
                    }
                }
            ),
        )

        val descriptor = CustomMcpToolDescriptorParser.parse(tool)

        assertEquals(
            ToolDescriptor(
                name = "Some tool",
                description = "Some tool description",
                requiredParameters = listOf(),
                optionalParameters = listOf(
                    ToolParameterDescriptor(
                        name = "nullableStringParam",
                        description = "Some nullable string parameter",
                        type = ToolParameterType.AnyOf(
                            listOf(
                                ToolParameterDescriptor(
                                    name = "",
                                    description = "Some nullable string parameter",
                                    type = ToolParameterType.String,
                                ),
                                ToolParameterDescriptor(
                                    name = "",
                                    description = "Some nullable string parameter",
                                    type = ToolParameterType.Null,
                                )
                            ).toTypedArray(),
                        ),
                    )
                ),
            ),
            descriptor,
        )
    }

    test("should parse parameter with defs") {
        val tool = Tool(
            name = "Some tool",
            description = "Some tool description",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("someParam") {
                        put($$"$ref", $$"#/$defs/SomeType")
                        put("description", "Some parameter")
                    }
                },
                defs = buildJsonObject {
                    putJsonObject("SomeType") {
                        put("type", "string")
                        putJsonArray("enum") {
                            add("value1")
                            add("value2")
                        }
                    }
                }
            ),
        )

        val descriptor = CustomMcpToolDescriptorParser.parse(tool)

        assertEquals(
            ToolDescriptor(
                name = "Some tool",
                description = "Some tool description",
                requiredParameters = listOf(),
                optionalParameters = listOf(
                    ToolParameterDescriptor(
                        name = "someParam",
                        description = "Some parameter",
                        type = ToolParameterType.Enum(
                            listOf("value1", "value2").toTypedArray(),
                        ),
                    )
                ),
            ),
            descriptor,
        )
    }
}
