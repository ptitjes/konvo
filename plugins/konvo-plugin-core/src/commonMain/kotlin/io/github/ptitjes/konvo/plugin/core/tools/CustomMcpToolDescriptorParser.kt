package io.github.ptitjes.konvo.plugin.core.tools

import ai.koog.agents.core.tools.*
import ai.koog.agents.mcp.*
import io.modelcontextprotocol.kotlin.sdk.types.*
import kotlinx.serialization.json.*
import io.modelcontextprotocol.kotlin.sdk.types.Tool as SDKTool

// TODO remove when Koog's McpToolDescriptorParser correctly parses nullable primitive types:
// e.g. `"maxResults":{"type":["integer","null"]}`
object CustomMcpToolDescriptorParser : McpToolDescriptorParser {
    // Maximum depth of recursive parsing
    private const val MAX_DEPTH = 30

    /**
     * Parses an MCP SDK Tool definition into tool descriptor format.
     *
     * This method extracts tool information (name, description, parameters) from an MCP SDK Tool
     * and converts it into a ToolDescriptor that can be used by the agent framework.
     *
     * @param sdkTool The MCP SDK Tool to parse.
     * @return A ToolDescriptor representing the MCP tool.
     */
    override fun parse(sdkTool: SDKTool): ToolDescriptor {
        // Parse all parameters from the input schema
        val parameters = parseParameters(sdkTool.inputSchema)

        // Get the list of required parameters
        val requiredParameters = sdkTool.inputSchema.required ?: emptyList()

        // Create a ToolDescriptor
        return ToolDescriptor(
            name = sdkTool.name,
            description = sdkTool.description.orEmpty(),
            requiredParameters = parameters.filter { requiredParameters.contains(it.name) },
            optionalParameters = parameters.filter { !requiredParameters.contains(it.name) },
        )
    }

    private fun parseParameters(inputSchema: ToolSchema): List<ToolParameterDescriptor> {
        val properties = inputSchema.properties ?: EmptyJsonObject

        return properties.mapNotNull { (name, element) ->
            require(element is JsonObject) { "Parameter $name must be a JSON object" }

            // Extract description from the element
            val description = element["description"]?.jsonPrimitive?.content.orEmpty()

            // Parse the parameter type
            val type = parseParameterType(element, inputSchema)

            // Create a ToolParameterDescriptor
            ToolParameterDescriptor(
                name = name,
                description = description,
                type = type
            )
        }
    }

    private fun parseParameterType(
        element: JsonObject,
        inputSchema: ToolSchema,
        depth: Int = 0,
    ): ToolParameterType {
        if (depth > MAX_DEPTH) {
            throw IllegalArgumentException(
                "Maximum recursion depth ($MAX_DEPTH) exceeded. " +
                        "This may indicate a circular reference in the parameter definition."
            )
        }

        val ref = element[$$"$ref"]
        if (ref != null) {
            val refPath = ref.jsonPrimitive.content
            val referencedSchema = inputSchema.resolveReference(refPath)
            return parseParameterType(referencedSchema, inputSchema, depth + 1)
        }

        // Extract the type string from the JSON object
        val typeElement = element["type"]

        if (typeElement is JsonArray) {
            val typeArray = typeElement.jsonArray.map { it.jsonPrimitive.content }
            val containsNull = typeArray.any { it == "null" }
            if (typeArray.size != 2 || !containsNull) {
                throw IllegalArgumentException("Invalid 'type' array: $typeArray")
            }

            val remainingType = typeArray.first { it != "null" }
            val primitiveType = when (remainingType) {
                "string" -> ToolParameterType.String
                "integer" -> ToolParameterType.Integer
                "number" -> ToolParameterType.Float
                "boolean" -> ToolParameterType.Boolean
                else -> throw IllegalArgumentException("Unsupported parameter type: $remainingType")
            }

            val description = element["description"]?.jsonPrimitive?.content.orEmpty()

            return ToolParameterType.AnyOf(
                arrayOf(
                    ToolParameterDescriptor(
                        name = "",
                        description = description,
                        type = primitiveType
                    ),
                    ToolParameterDescriptor(
                        name = "",
                        description = description,
                        type = ToolParameterType.Null
                    )
                )
            )
        }

        val typeStr = typeElement?.jsonPrimitive?.content

        val anyOf = element["anyOf"]?.jsonArray
        if (anyOf != null) {
            /**
             * anyOf with multiple types.
             * Schema example:
             * {
             *   "anyOfParam": {
             *     "anyOf": [
             *       { "type": "string" },
             *       { "type": "number" }
             *     ],
             *     "title": "string or number parameter"
             *   }
             * }
             */
            return ToolParameterType.AnyOf(
                types = anyOf.map { it.jsonObject }.map {
                    ToolParameterDescriptor(
                        name = "",
                        description = it["description"]?.jsonPrimitive?.content.orEmpty(),
                        type = parseParameterType(it.jsonObject, inputSchema)
                    )
                }.toTypedArray()
            )
        }

        /**
         * Special case for enum string types.
         * Schema example:
         * {
         *   "enumParam": {
         *     "enum": [
         *       "value1",
         *       "value2"
         *     ],
         *     "title": "Enum string parameter"
         *   }
         * }
         */
        val enum = element["enum"]?.jsonArray
        if (enum != null && enum.isNotEmpty()) {
            return ToolParameterType.Enum(enum.map { it.jsonPrimitive.content }.toTypedArray())
        }

        if (typeStr == null) {
            error("Parameter must have type property")
        }

        // Convert the type string to a ToolParameterType
        return when (typeStr.lowercase()) {
            // Primitive types
            "string" -> ToolParameterType.String

            "integer" -> ToolParameterType.Integer

            "number" -> ToolParameterType.Float

            "boolean" -> ToolParameterType.Boolean

            "enum" -> ToolParameterType.Enum(
                element.getValue("enum").jsonArray.map { it.jsonPrimitive.content }.toTypedArray()
            )

            // Array type
            "array" -> {
                val items = element["items"]?.jsonObject
                    ?: throw IllegalArgumentException("Array type parameters must have items property")

                val itemType = parseParameterType(items, inputSchema, depth + 1)

                ToolParameterType.List(itemsType = itemType)
            }

            // Object type
            "object" -> {
                val properties = element["properties"]?.let { properties ->
                    val rawProperties = properties.jsonObject
                    rawProperties.map { (name, property) ->
                        // Description is optional
                        val description = property.jsonObject["description"]?.jsonPrimitive?.content.orEmpty()
                        ToolParameterDescriptor(
                            name,
                            description,
                            parseParameterType(property.jsonObject, inputSchema, depth + 1)
                        )
                    }
                } ?: emptyList()

                val required = element["required"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()

                val additionalProperties = if ("additionalProperties" in element) {
                    when (element.getValue("additionalProperties")) {
                        is JsonPrimitive -> element.getValue("additionalProperties").jsonPrimitive.boolean
                        is JsonObject -> true
                        else -> null
                    }
                } else {
                    null
                }

                val additionalPropertiesType = if ("additionalProperties" in element) {
                    when (element.getValue("additionalProperties")) {
                        is JsonObject -> parseParameterType(
                            element.getValue("additionalProperties").jsonObject,
                            inputSchema,
                            depth + 1
                        )

                        else -> null
                    }
                } else {
                    null
                }

                ToolParameterType.Object(
                    properties = properties,
                    requiredProperties = required,
                    additionalPropertiesType = additionalPropertiesType,
                    additionalProperties = additionalProperties
                )
            }

            "null" -> ToolParameterType.Null

            // Unsupported type
            else -> throw IllegalArgumentException("Unsupported parameter type: $typeStr")
        }
    }
}

private const val DEF_PREFIX = $$"#/$defs/"

private fun ToolSchema.resolveReference(refPath: String): JsonObject {
    if (!refPath.startsWith(DEF_PREFIX)) error("Invalid reference path: $refPath")

    val definitions = defs ?: error("No definitions available")

    val id = refPath.removePrefix(DEF_PREFIX)
    return definitions.getValue(id).jsonObject
}
