package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.ai.spi.ToolCard

/**
 * A selector for tools.
 *
 * @param selectedTools The currently selected tools
 * @param onToolsSelected Callback for when tools are selected
 * @param tools List of available tools
 * @param modifier The modifier to apply to this component
 */
@Composable
fun ToolSelector(
    selectedTools: List<ToolCard>,
    onToolsSelected: (List<ToolCard>) -> Unit,
    tools: List<ToolCard>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Tools",
            style = MaterialTheme.typography.titleSmall
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tools.forEach { tool ->
                FilterChip(
                    selected = selectedTools.contains(tool),
                    onClick = {
                        if (selectedTools.contains(tool)) {
                            onToolsSelected(selectedTools - tool)
                        } else {
                            onToolsSelected(selectedTools + tool)
                        }
                    },
                    label = { Text(tool.name) }
                )
            }
        }
    }
}
