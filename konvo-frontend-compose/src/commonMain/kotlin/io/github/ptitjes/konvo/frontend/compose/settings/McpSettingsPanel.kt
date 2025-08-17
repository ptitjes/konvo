package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.ai.mcp.*
import io.github.ptitjes.konvo.core.settings.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import io.github.ptitjes.konvo.frontend.compose.components.settings.*
import kotlin.time.Duration.Companion.seconds

private enum class McpTransportType { Stdio, Sse }

@Composable
fun McpSettingsPanel(
    settings: McpSettings,
    updateSettings: (updater: (previous: McpSettings) -> McpSettings) -> Unit,
) {
    fun addServer(newName: String, newSpec: ServerSpecification) {
        updateSettings { previous -> previous.copy(servers = previous.servers + (newName to newSpec)) }
    }

    fun updateServer(name: String, transform: (ServerSpecification) -> ServerSpecification) {
        updateSettings { previous ->
            val current = previous.servers[name] ?: return@updateSettings previous
            previous.copy(servers = previous.servers + (name to transform(current)))
        }
    }

    fun renameServer(oldName: String, newName: String) {
        if (newName.isBlank() || oldName == newName) return
        updateSettings { previous ->
            val spec = previous.servers[oldName] ?: return@updateSettings previous
            previous.copy(servers = previous.servers - oldName + (newName to spec))
        }
    }

    fun removeServer(name: String) {
        updateSettings { previous -> previous.copy(servers = previous.servers - name) }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsBox(
            title = "Configured MCP servers",
            description = "Add, remove, and edit MCP servers.",
        ) {
            if (settings.servers.isEmpty()) {
                Text(
                    text = "No MCP servers configured.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    settings.servers.entries.forEachIndexed { index, entry ->
                        val name = entry.key
                        val spec = entry.value
                        ServerEditor(
                            name = name,
                            specification = spec,
                            otherNames = settings.servers.keys - name,
                            onRename = { newName -> renameServer(name, newName) },
                            onChange = { newSpec -> updateServer(name) { _ -> newSpec } },
                            onRemove = { removeServer(name) },
                        )
                        if (index < settings.servers.size - 1) HorizontalDivider()
                    }
                }
            }
        }

        AddServerBox(
            existingNames = settings.servers.keys,
            onAdd = { newName, newSpec -> addServer(newName, newSpec) }
        )
    }
}

@Composable
private fun ServerEditor(
    name: String,
    specification: ServerSpecification,
    otherNames: Set<String>,
    onRename: (String) -> Unit,
    onChange: (ServerSpecification) -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = name,
                onValueChange = { newName -> onRename(newName) },
                label = { Text("Name") },
                isError = name.isBlank() || otherNames.contains(name),
                singleLine = true,
            )

            var transportType by remember(specification.transport) {
                mutableStateOf(specification.transport.toType())
            }

            GenericSelector(
                label = "Transport",
                selectedItem = transportType,
                onSelectItem = { selected ->
                    transportType = selected
                    val newTransport = when (selected) {
                        McpTransportType.Stdio -> TransportSpecification.Stdio
                        McpTransportType.Sse -> when (val t = specification.transport) {
                            is TransportSpecification.Sse -> t
                            else -> TransportSpecification.Sse(url = "", reconnectionTime = null)
                        }
                    }
                    onChange(specification.copy(transport = newTransport))
                },
                options = McpTransportType.entries,
                itemLabeler = { it.name },
                modifier = Modifier.widthIn(min = 180.dp).weight(0.7f),
            )

            IconButton(onClick = onRemove) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove server")
            }
        }

        when (val transport = specification.transport) {
            TransportSpecification.Stdio -> {
                // No additional fields for stdio
            }

            is TransportSpecification.Sse -> {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = transport.url.orEmpty(),
                    onValueChange = { newUrl -> onChange(specification.copy(transport = transport.copy(url = newUrl))) },
                    label = { Text("SSE URL") },
                    singleLine = true,
                )

                var reconnectionTimeText by remember(transport.reconnectionTime) {
                    mutableStateOf(
                        transport.reconnectionTime?.inWholeSeconds?.toString().orEmpty()
                    )
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = reconnectionTimeText,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }
                        reconnectionTimeText = filtered
                        val reconnectionTime = filtered.toLongOrNull()?.takeIf { it >= 0 }?.seconds
                        onChange(specification.copy(transport = transport.copy(reconnectionTime = reconnectionTime)))
                    },
                    label = { Text("Reconnection time (seconds)") },
                    singleLine = true,
                )
            }
        }

        // Process configuration
        val hasProcess = specification.process != null

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "Run as process")
            Switch(
                checked = hasProcess,
                onCheckedChange = { enabled ->
                    val newSpec = if (enabled) {
                        specification.copy(
                            process = ProcessSpecification(
                                command = listOf("program"),
                                environment = emptyMap()
                            )
                        )
                    } else {
                        specification.copy(process = null)
                    }
                    onChange(newSpec)
                }
            )
        }

        if (hasProcess) {
            val process = specification.process!!

            var commandText by remember(process) { mutableStateOf(process.command.joinToString(" ")) }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = commandText,
                onValueChange = { newValue ->
                    commandText = newValue
                    val parts = newValue.split(" ").filter { it.isNotBlank() }
                    onChange(specification.copy(process = process.copy(command = parts)))
                },
                label = { Text("Command (space-separated)") },
                singleLine = true,
            )

            var environmentText by remember(process) {
                mutableStateOf(process.environment?.entries?.joinToString("; ") { "${it.key}=${it.value}" }.orEmpty())
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = environmentText,
                onValueChange = { newValue ->
                    environmentText = newValue
                    val environmentMap = parseEnv(newValue)
                    onChange(specification.copy(process = process.copy(environment = environmentMap.ifEmpty { null })))
                },
                label = { Text("Environment (key=value; key2=value2)") },
                singleLine = true,
            )
        }
    }
}

private fun TransportSpecification.toType(): McpTransportType = when (this) {
    TransportSpecification.Stdio -> McpTransportType.Stdio
    is TransportSpecification.Sse -> McpTransportType.Sse
}

@Composable
private fun AddServerBox(
    existingNames: Set<String>,
    onAdd: (name: String, specification: ServerSpecification) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var transportType by remember { mutableStateOf(McpTransportType.Stdio) }
    var sseUrl by remember { mutableStateOf("") }
    var sseReconnectionTimeText by remember { mutableStateOf("") }
    var addProcess by remember { mutableStateOf(false) }
    var commandText by remember { mutableStateOf("") }
    var environmentText by remember { mutableStateOf("") }

    fun isValid(): Boolean {
        if (name.isBlank() || name in existingNames) return false
        if (transportType == McpTransportType.Sse && sseUrl.isBlank()) return false
        if (addProcess && commandText.isBlank()) return false
        return true
    }

    SettingsBox(
        title = "Add server",
        description = "Create a new MCP server entry.",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    isError = name.isBlank() || name in existingNames,
                )

                GenericSelector(
                    label = "Transport",
                    selectedItem = transportType,
                    onSelectItem = { transportType = it },
                    options = McpTransportType.entries,
                    itemLabeler = { it.name },
                    modifier = Modifier.widthIn(min = 180.dp).weight(0.7f),
                )
            }

            if (transportType == McpTransportType.Sse) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = sseUrl,
                    onValueChange = { sseUrl = it },
                    label = { Text("SSE URL") },
                    singleLine = true,
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = sseReconnectionTimeText,
                    onValueChange = { sseReconnectionTimeText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Reconnection time (seconds)") },
                    singleLine = true,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = "Run as process")
                Switch(checked = addProcess, onCheckedChange = { addProcess = it })
            }

            if (addProcess) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = commandText,
                    onValueChange = { commandText = it },
                    label = { Text("Command (space-separated)") },
                    singleLine = true,
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = environmentText,
                    onValueChange = { environmentText = it },
                    label = { Text("Environment (key=value; key2=value2)") },
                    singleLine = true,
                )
            }

            Button(
                enabled = isValid(),
                onClick = {
                    val transportSpecification = when (transportType) {
                        McpTransportType.Stdio -> TransportSpecification.Stdio
                        McpTransportType.Sse -> TransportSpecification.Sse(
                            url = sseUrl,
                            reconnectionTime = sseReconnectionTimeText.toLongOrNull()?.seconds
                        )
                    }

                    val process = if (addProcess) {
                        val cmd = commandText.split(" ").filter { it.isNotBlank() }

                        ProcessSpecification(
                            command = cmd,
                            environment = parseEnv(environmentText).ifEmpty { null },
                        )
                    } else null

                    val serverSpecification = ServerSpecification(transport = transportSpecification, process = process)
                    onAdd(name, serverSpecification)

                    // Reset
                    name = ""
                    transportType = McpTransportType.Stdio
                    sseUrl = ""
                    sseReconnectionTimeText = ""
                    addProcess = false
                    commandText = ""
                    environmentText = ""
                }
            ) {
                Text("Add server")
            }
        }
    }
}

private fun parseEnv(text: String): Map<String, String> {
    if (text.isBlank()) return emptyMap()
    return text.split(';')
        .mapNotNull { entry ->
            val trimmed = entry.trim()
            if (trimmed.isBlank()) return@mapNotNull null
            val index = trimmed.indexOf('=')
            if (index <= 0 || index >= trimmed.lastIndex) return@mapNotNull null
            val key = trimmed.take(index).trim()
            val value = trimmed.substring(index + 1).trim()
            if (key.isEmpty()) null else key to value
        }
        .toMap()
}
