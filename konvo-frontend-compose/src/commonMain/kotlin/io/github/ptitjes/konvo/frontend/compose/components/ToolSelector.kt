package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.ai.spi.*

/**
 * A selector for tools.
 *
 * @param selectedTools The currently selected tools
 * @param onToolsSelected Callback for when tools are selected
 * @param tools List of available tools
 * @param modifier The modifier to apply to this component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolSelector(
    selectedTools: List<ToolCard>,
    onToolsSelected: (List<ToolCard>) -> Unit,
    tools: List<ToolCard>,
    modifier: Modifier = Modifier,
) {
    OutlineBox(
        label = "Tools",
        modifier = modifier,
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
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
                    label = { Text(tool.name) },
                )
            }
        }
    }
}

@Composable
fun OutlineBox(
    label: String,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier.padding(top = 8.dp).fillMaxWidth().border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(4.dp)
            )
        ) {
            Box(
                modifier = Modifier.padding(16.dp, 8.dp),
            ) {
                content()
            }
        }

        Row(
            modifier = Modifier.offset(x = 12.dp, y = (-2).dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}
