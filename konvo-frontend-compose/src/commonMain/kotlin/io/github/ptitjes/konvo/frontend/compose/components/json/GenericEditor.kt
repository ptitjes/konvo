package io.github.ptitjes.konvo.frontend.compose.components.json

import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.xemantic.ai.tool.schema.*
import com.xemantic.ai.tool.schema.generator.*
import kotlinx.serialization.json.*

@Composable
inline fun <reified T> GenericEditor(
    value: T,
    label: String? = null,
    modifier: Modifier = Modifier,
    crossinline onValueChange: (T) -> Unit,
) {
    val jsonSchema = remember { jsonSchemaOf<T>() as ObjectSchema }
    val jsonObject = remember(value) { Json.encodeToJsonElement(value) as JsonObject }

    JsonObjectEditor(
        modifier = modifier,
        schema = jsonSchema,
        label = label,
        value = jsonObject,
        onValueChange = { value -> onValueChange(Json.decodeFromJsonElement<T>(value)) },
    )
}

@Composable
inline fun <reified T> GenericEditor(
    value: List<T>,
    label: String? = null,
    modifier: Modifier = Modifier,
    crossinline onValueChange: (List<T>) -> Unit,
) {
    val jsonSchema = remember { jsonSchemaOf<List<T>>() as ArraySchema }
    val jsonObject = remember(value) { Json.encodeToJsonElement(value) as JsonArray }

    JsonArrayEditor(
        modifier = modifier,
        schema = jsonSchema,
        label = label,
        value = jsonObject,
        onValueChange = { value -> onValueChange(Json.decodeFromJsonElement<List<T>>(value)) },
    )
}
