package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.*
import com.mikepenz.markdown.m3.*
import kotlinx.serialization.json.*

@Composable
fun ToolArgumentsTable(
    arguments: Map<String, JsonElement>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
    ) {
        Column {
            val headerColumnWidth = remember { mutableStateOf<Int?>(null) }

            arguments.entries.forEach { (name, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "$name:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.withSharedWidth(headerColumnWidth)
                    )

                    val code = remember(value) {
                        prettyJson.encodeToString(JsonElement.serializer(), value)
                    }

                    Markdown(
                        content = "```json\n$code\n```",
                        typography = markdownTypography(code = MaterialTheme.typography.bodyMedium),
                        colors = markdownColor(text = MaterialTheme.colorScheme.onSurface),
                    )
                }
            }
        }
    }
}

private val prettyJson = Json { prettyPrint = true }

private fun Modifier.withSharedWidth(headerColumnWidth: MutableState<Int?>) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)

    val existingWidth = headerColumnWidth.value ?: 0
    val maxWidth = maxOf(existingWidth, placeable.width)

    if (maxWidth > existingWidth) {
        headerColumnWidth.value = maxWidth
    }

    layout(width = maxWidth, height = placeable.height) {
        placeable.placeRelative(0, 0)
    }
}

