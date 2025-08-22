package io.github.ptitjes.konvo.frontend.compose.tools

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.tools.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*

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
        label = strings.tools.panelLabel,
        modifier = modifier,
    ) {
        if (tools.isEmpty()) {
            Text(
                text = strings.tools.emptyMessage,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
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
}

