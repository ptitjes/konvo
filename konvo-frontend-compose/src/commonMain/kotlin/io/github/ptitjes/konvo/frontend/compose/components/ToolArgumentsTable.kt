package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
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
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
    ) {
        Column {
            arguments.entries.forEachIndexed { index, (name, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "$name:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(160.dp).padding(horizontal = 8.dp)
                    )

                    val code = remember(value) {
                        prettyJson.encodeToString(JsonElement.serializer(), value)
                    }

                    Markdown(
                        content = "```json\n$code\n```",
                        colors = markdownColor(text = MaterialTheme.colorScheme.onSurface),
                    )
                }

                if (index < arguments.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

private val prettyJson = Json { prettyPrint = true }
