package io.github.ptitjes.konvo.frontend.compose.toolkit.json

import com.xemantic.ai.tool.schema.*

internal fun WithDefinitions.maybeResolveRef(schema: JsonSchema): JsonSchema =
    if (schema is JsonSchema.Ref) resolveRef(schema) else schema

internal fun WithDefinitions.resolveRef(schema: JsonSchema.Ref): JsonSchema {
    val definitions = definitions
    val refString = schema.ref
    if (definitions == null) error("No definitions at root level")
    else if (!refString.startsWith("#/definitions/")) error("Invalid ref $refString")
    else return definitions[refString.removePrefix("#/definitions/")]
        ?: error("No definition found for ref $refString")
}
