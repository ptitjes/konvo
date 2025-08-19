package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*

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
    servers: Set<String>,
    modifier: Modifier = Modifier,
) {
    OutlineBox(
        label = "MCP Servers",
        modifier = modifier,
    ) {
        if (servers.isEmpty()) {
            Text(
                text = "No MCP servers available",
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                servers.sortedBy { it }.forEach { server ->
                    FilterChip(
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
