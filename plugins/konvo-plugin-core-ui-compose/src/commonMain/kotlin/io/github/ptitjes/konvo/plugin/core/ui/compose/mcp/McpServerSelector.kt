package io.github.ptitjes.konvo.plugin.core.ui.compose.mcp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*

/**
 * A selector for MCP servers.
 *
 * @param selectedServers The currently selected servers
 * @param onServersSelected Callback for when servers are selected
 * @param servers List of available servers
 * @param modifier The modifier to apply to this component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McpServerSelector(
    selectedServers: Set<String>,
    onServersSelected: (Set<String>) -> Unit,
    onGoToSettingsClick: () -> Unit,
    servers: Set<String>,
    modifier: Modifier = Modifier,
) {
    OutlineBox(
        label = i18n.mcp.selectorLabel,
        modifier = modifier.heightIn(min = 64.dp),
    ) {
        if (servers.isEmpty()) {
            UnavailabilityPlaceholder(
                modifier = Modifier.padding(horizontal = 8.dp),
                unavailabilityText = i18n.mcp.selectorEmpty,
                onGoToSettings = onGoToSettingsClick,
            )
        } else {
            FlowRow(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                servers.sortedBy { it }.forEach { server ->
                    ElevatedFilterChip(
                        selected = selectedServers.contains(server),
                        onClick = {
                            if (selectedServers.contains(server)) {
                                onServersSelected(selectedServers - server)
                            } else {
                                onServersSelected(selectedServers + server)
                            }
                        },
                        label = { Text(server) },
                    )
                }
            }
        }
    }
}
