package io.github.ptitjes.konvo.frontend.compose.components.json

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.xemantic.ai.tool.schema.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import kotlinx.serialization.json.*

@Composable
fun JsonEditor(
    schema: JsonSchema,
    rootSchema: WithDefinitions,
    label: String? = schema.title,
    value: JsonElement,
    onValueChange: (JsonElement) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (schema) {
        is ObjectSchema -> JsonObjectEditor(
            schema = schema,
            rootSchema = rootSchema,
            label = label,
            value = value.jsonObject,
            onValueChange = onValueChange,
            modifier = modifier.grouped(),
        )

        is ArraySchema -> JsonArrayEditor(
            schema = schema,
            rootSchema = rootSchema,
            label = label,
            value = value.jsonArray,
            onValueChange = { onValueChange(it) },
            modifier = modifier.grouped(),
        )

        is JsonSchema.Ref -> {
            val nestedSchema = rootSchema.resolveRef(schema)

            JsonEditor(
                modifier = modifier,
                label = label,
                schema = nestedSchema,
                rootSchema = rootSchema,
                value = value,
                onValueChange = onValueChange,
            )
        }

        is StringSchema -> JsonStringEditor(
            label = label,
            value = value.jsonPrimitive,
            onValueChange = onValueChange,
        )

        is BooleanSchema -> JsonBooleanEditor(
            modifier = modifier,
            label = label,
            value = value.jsonPrimitive,
            onValueChange = onValueChange,
        )

        is IntegerSchema -> JsonIntegerEditor(
            modifier = modifier,
            label = label,
            value = value.jsonPrimitive,
            onValueChange = onValueChange,
        )

        is NumberSchema -> JsonNumberEditor(
            modifier = modifier,
            label = label,
            value = value.jsonPrimitive,
            onValueChange = onValueChange,
        )

        is JsonSchema.Const -> {}
    }
}

@Composable
fun JsonObjectEditor(
    schema: ObjectSchema,
    rootSchema: WithDefinitions = schema,
    label: String? = null,
    value: JsonObject,
    onValueChange: (JsonObject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val oneOf = schema.oneOf
    val allOf = schema.allOf
    when {
        oneOf != null && oneOf.isNotEmpty() -> JsonOneOfEditor(
            modifier = modifier,
            label = label,
            schemas = oneOf,
            rootSchema = rootSchema,
            value = value,
            onValueChange = onValueChange,
        )

        allOf != null && allOf.isNotEmpty() -> JsonAllOfEditor(
            modifier = modifier,
            schemas = allOf,
            rootSchema = rootSchema,
            value = value,
            onValueChange = onValueChange,
        )

        else -> JsonPropertiesEditor(
            modifier = modifier,
            label = label,
            schema = schema,
            rootSchema = rootSchema,
            value = value,
            onValueChange = onValueChange,
        )
    }
}

@Composable
private fun JsonPropertiesEditor(
    label: String? = null,
    schema: ObjectSchema,
    rootSchema: WithDefinitions,
    value: JsonObject,
    onValueChange: (JsonObject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val properties = schema.properties
    val required = schema.required

    properties?.let { properties ->
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (label != null) Text(text = label)

            properties.forEach { (name, propertySchema) ->
                val currentValue = value[name] ?: defaultJsonElement(propertySchema, rootSchema)

                key(name) {
                    JsonEditor(
                        schema = propertySchema,
                        rootSchema = rootSchema,
                        value = currentValue,
                        onValueChange = { newValue -> onValueChange(value.withUpdatedValue(name, newValue)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun JsonOneOfEditor(
    label: String?,
    schemas: List<JsonSchema>,
    rootSchema: WithDefinitions,
    value: JsonObject,
    onValueChange: (JsonObject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedCaseIndex = remember(value) { oneOfCaseIndex(schemas, value, rootSchema) }
    val selectedSchema = remember(selectedCaseIndex) { schemas[selectedCaseIndex] as ObjectSchema }

    Column(
        modifier = modifier,
    ) {
        GenericSelector(
            label = label ?: "",
            selectedItem = selectedCaseIndex,
            onSelectItem = { index ->
                if (index != selectedCaseIndex) {
                    val newSchema = rootSchema.maybeResolveRef(schemas[index]) as ObjectSchema
                    onValueChange(defaultJsonElement(newSchema, rootSchema) as JsonObject)
                }
            },
            options = schemas.indices.toList(),
            itemLabeler = { index -> oneOfCaseLabel(schemas[index], index, rootSchema) },
        )

        JsonObjectEditor(
            schema = selectedSchema,
            rootSchema = rootSchema,
            value = value,
            onValueChange = onValueChange,
        )
    }
}

private fun oneOfCaseIndex(schemas: List<JsonSchema>, value: JsonObject, rootSchema: WithDefinitions): Int {
    val index = schemas.indexOfFirst { schema ->
        val (discriminantProperty, discriminantValue) = oneOfCaseDiscriminant(schema, rootSchema)
        value[discriminantProperty]?.jsonPrimitive?.content == discriminantValue
    }
    if (index == -1) error("No oneOf case found for value $value")
    return index
}

private fun oneOfCaseDiscriminant(oneOfCaseSchema: JsonSchema, rootSchema: WithDefinitions): Pair<String, String> {
    return when (oneOfCaseSchema) {
        is JsonSchema.Ref -> oneOfCaseDiscriminant(rootSchema.resolveRef(oneOfCaseSchema), rootSchema)

        is ObjectSchema -> {
            val allOf = oneOfCaseSchema.allOf
            // Assume the first member of the allOf is a const representing the case discriminator
            // Assume the second member of the allOf is a ref to the actual schema for the case
            allOf?.getOrNull(0)?.let { discriminantSchema ->
                val properties = (discriminantSchema as ObjectSchema).properties
                properties?.entries?.first()?.let { (name, constSchema) ->
                    val constValue = (constSchema as? JsonSchema.Const)?.const!!
                    name to constValue
                }
            } ?: error("Illegal oneOf case schema: $oneOfCaseSchema, missing const in first allOf member")
        }

        else -> error("Invalid oneOf case schema: $oneOfCaseSchema")
    }
}

private fun oneOfCaseLabel(oneOfCaseSchema: JsonSchema, index: Int, rootSchema: WithDefinitions): String {
    val title = when (oneOfCaseSchema) {
        is JsonSchema.Ref -> oneOfCaseLabel(rootSchema.resolveRef(oneOfCaseSchema), index, rootSchema)

        is ObjectSchema -> {
            val allOf = oneOfCaseSchema.allOf
            // Assume the first member of the allOf is a const representing the case discriminator
            // Assume the second member of the allOf is a ref to the actual schema for the case
            allOf?.getOrNull(1)?.let { objectSchemaTitle(it, rootSchema) }
        }

        else -> oneOfCaseSchema.title
    }
    return title ?: "Option ${index + 1}"
}

private fun objectSchemaTitle(schema: JsonSchema, rootSchema: WithDefinitions): String? {
    return when (schema) {
        is JsonSchema.Ref -> objectSchemaTitle(rootSchema.resolveRef(schema), rootSchema)
        is ObjectSchema -> schema.title
        else -> null
    }
}

@Composable
private fun JsonAllOfEditor(
    schemas: List<JsonSchema>,
    rootSchema: WithDefinitions,
    value: JsonObject,
    onValueChange: (JsonObject) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        schemas.forEach { subSchema ->
            val resolved = if (subSchema is JsonSchema.Ref) rootSchema.resolveRef(subSchema) else subSchema
            when (resolved) {
                is ObjectSchema -> JsonPropertiesEditor(
                    schema = resolved,
                    rootSchema = rootSchema,
                    value = value,
                    onValueChange = onValueChange,
                )

                else -> error("Invalid allOf member: $resolved")
            }
        }
    }
}

@Composable
fun JsonArrayEditor(
    schema: ArraySchema,
    rootSchema: WithDefinitions = schema,
    label: String?,
    value: JsonArray,
    onValueChange: (JsonArray) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemSchema: JsonSchema = remember(schema) { rootSchema.maybeResolveRef(schema.items) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (label != null) Text(text = label)

        value.forEachIndexed { index, element ->
            JsonArrayItemEditor(
                itemSchema = itemSchema,
                rootSchema = rootSchema,
                element = element,
                onItemChange = { updated ->
                    val newList = value.toMutableList().apply { set(index, updated) }.toList()
                    onValueChange(JsonArray(newList))
                },
                onItemDelete = {
                    val newList = value.toMutableList().apply { removeAt(index) }.toList()
                    onValueChange(JsonArray(newList))
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            IconButton(
                onClick = {
                    val defaultItem = defaultJsonElement(itemSchema, rootSchema)
                    onValueChange(JsonArray(value + defaultItem))
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                )
            }
        }
    }
}

@Composable
private fun JsonArrayItemEditor(
    itemSchema: JsonSchema,
    rootSchema: WithDefinitions,
    element: JsonElement,
    onItemChange: (JsonElement) -> Unit,
    onItemDelete: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            JsonEditor(
                schema = itemSchema,
                rootSchema = rootSchema,
                value = element,
                onValueChange = onItemChange,
            )
        }

        IconButton(
            modifier = Modifier.align(Alignment.Top),
            onClick = onItemDelete,
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove",
            )
        }
    }
}

@Composable
private fun JsonStringEditor(
    label: String?,
    value: JsonPrimitive,
    onValueChange: (JsonPrimitive) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        label = { if (label != null) Text(label) },
        value = value.content,
        onValueChange = { newValue -> onValueChange(JsonPrimitive(newValue)) },
    )
}

@Composable
private fun JsonBooleanEditor(
    label: String?,
    value: JsonPrimitive,
    onValueChange: (JsonPrimitive) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = label ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Switch(
            checked = value.boolean,
            onCheckedChange = { checked -> onValueChange(JsonPrimitive(checked)) },
        )
    }
}

@Composable
private fun JsonIntegerEditor(
    label: String?,
    value: JsonPrimitive,
    onValueChange: (JsonPrimitive) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        label = { if (label != null) Text(label) },
        value = value.long.toString(),
        onValueChange = { newValue ->
            newValue.toLongOrNull()?.let { onValueChange(JsonPrimitive(it)) }
        },
    )
}

@Composable
private fun JsonNumberEditor(
    label: String?,
    value: JsonPrimitive,
    onValueChange: (JsonPrimitive) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        label = { if (label != null) Text(label) },
        value = value.double.toString(),
        onValueChange = { newValue ->
            newValue.toDoubleOrNull()?.let { onValueChange(JsonPrimitive(it)) }
        },
    )
}

private fun JsonObject.withUpdatedValue(name: String, value: JsonElement): JsonObject {
    return JsonObject(this.mapValues { (key, currentValue) ->
        if (key == name) value else currentValue
    })
}

private fun Modifier.grouped() = composed {
    drawLeftBorder(
        width = 2.dp,
        color = MaterialTheme.colorScheme.onBackground,
    ).padding(start = 16.dp)
}

private fun Modifier.drawLeftBorder(
    width: Dp,
    color: Color,
) = this.drawWithContent {
    val widthPx = width.toPx()

    drawContent()
    drawLine(
        color = color,
        start = Offset(widthPx / 2, 0f),
        end = Offset(widthPx / 2, size.height),
        strokeWidth = widthPx,
    )
}
