package io.github.ptitjes.konvo.frontend.compose.mcp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
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

    var sheetState by remember { mutableStateOf<McpServersSheetState>(McpServersSheetState.Closed) }
    var serverPendingDeletion by remember { mutableStateOf<String?>(null) }

    SettingsBox(
        title = strings.mcp.configuredServersTitle,
        description = strings.mcp.configuredServersDescription,
        trailingContent = {
            FilledTonalIconButton(onClick = { sheetState = McpServersSheetState.Adding }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = strings.mcp.addServerAria)
            }
        },
        bottomContent = {
            if (settings.servers.isEmpty()) {
                Text(
                    text = strings.mcp.noServersMessage,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    settings.servers.entries.sortedBy { it.key }.forEach { (name, specification) ->
                        Surface(
                            tonalElevation = 2.dp,
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = specification.transport.toType().name,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }

                                IconButton(onClick = { sheetState = McpServersSheetState.Editing(name) }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = strings.mcp.editServerAria
                                    )
                                }

                                IconButton(onClick = { serverPendingDeletion = name }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = strings.mcp.deleteServerAria
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    )

    // Deletion confirmation dialog
    serverPendingDeletion?.let { nameToDelete ->
        AlertDialog(
            onDismissRequest = { serverPendingDeletion = null },
            title = { Text(strings.mcp.deleteServerDialogTitle) },
            text = { Text(strings.mcp.deleteServerDialogText(nameToDelete)) },
            confirmButton = {
                TextButton(onClick = {
                    removeServer(nameToDelete)
                    serverPendingDeletion = null
                    val currentSheet = sheetState
                    if (currentSheet is McpServersSheetState.Editing && currentSheet.name == nameToDelete) {
                        sheetState = McpServersSheetState.Closed
                    }
                }) {
                    Text(strings.mcp.deleteConfirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { serverPendingDeletion = null }) { Text(strings.mcp.cancel) }
            },
        )
    }

    if (sheetState !is McpServersSheetState.Closed) {
        ModalBottomSheet(
            onDismissRequest = { sheetState = McpServersSheetState.Closed },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            when (val sheet = sheetState) {
                is McpServersSheetState.Adding -> {
                    AddServerSheetContent(
                        existingNames = settings.servers.keys,
                        onAdd = { newName, newSpec ->
                            addServer(newName, newSpec)
                            sheetState = McpServersSheetState.Closed
                        },
                    )
                }

                is McpServersSheetState.Editing -> {
                    val currentName = sheet.name
                    val spec = settings.servers[currentName]
                    if (spec != null) {
                        EditServerSheetContent(
                            name = currentName,
                            specification = spec,
                            otherNames = settings.servers.keys - currentName,
                            onRename = { newName ->
                                renameServer(currentName, newName)
                                sheetState = McpServersSheetState.Editing(newName)
                            },
                            onChange = { newSpec -> updateServer(currentName) { _ -> newSpec } },
                            onRemove = {
                                serverPendingDeletion = currentName
                            },
                        )
                    } else {
                        // In case the server was renamed/removed, close the sheet
                        sheetState = McpServersSheetState.Closed
                    }
                }

                is McpServersSheetState.Closed -> {}
            }
        }
    }
}

@Composable
private fun EditServerSheetContent(
    name: String,
    specification: ServerSpecification,
    otherNames: Set<String>,
    onRename: (String) -> Unit,
    onChange: (ServerSpecification) -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                label = { Text(strings.mcp.nameLabel) },
                isError = name.isBlank() || otherNames.contains(element = name),
                singleLine = true,
            )

            var transportType by remember(specification.transport) {
                mutableStateOf(specification.transport.toType())
            }

            GenericSelector(
                label = strings.mcp.transportLabel,
                selectedItem = transportType,
                onSelectItem = { selected ->
                    transportType = selected
                    val newTransport = when (selected) {
                        McpTransportType.Stdio -> TransportSpecification.Stdio
                        McpTransportType.Sse -> when (val t = specification.transport) {
                            is TransportSpecification.Sse -> t
                            else -> TransportSpecification.Sse(url = "")
                        }
                    }
                    onChange(specification.copy(transport = newTransport))
                },
                options = McpTransportType.entries,
                itemLabeler = { it.name },
                modifier = Modifier.widthIn(min = 180.dp).weight(0.7f),
            )

            IconButton(
                modifier = Modifier.offset(y = 4.dp),
                onClick = onRemove,
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = strings.mcp.removeServerAria)
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
                    label = { Text(strings.mcp.sseUrlLabel) },
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
                    label = { Text(strings.mcp.reconnectionTimeLabel) },
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
            Text(
                modifier = Modifier.weight(1f),
                text = strings.mcp.runAsProcessLabel,
            )
            Switch(
                checked = hasProcess, onCheckedChange = { enabled ->
                    val newSpec = if (enabled) {
                        specification.copy(
                            process = ProcessSpecification(
                                command = listOf(),
                                environment = emptyMap(),
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

            var commandText by remember(process) { mutableStateOf(process.command.buildCommandString()) }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = commandText,
                onValueChange = { newValue ->
                    commandText = newValue
                    val parts = newValue.parseCommandString()
                    onChange(specification.copy(process = process.copy(command = parts)))
                },
                label = { Text(strings.mcp.commandLabel) },
                singleLine = true,
            )

            var environmentText by remember(process) {
                mutableStateOf(process.environment?.buildEnvironmentString().orEmpty())
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = environmentText,
                onValueChange = { newValue ->
                    environmentText = newValue
                    val environment = newValue.parseEnvironmentString().ifEmpty { null }
                    onChange(specification.copy(process = process.copy(environment = environment)))
                },
                label = { Text(strings.mcp.environmentLabel) },
                singleLine = true,
            )
        }
    }
}

@Composable
private fun AddServerSheetContent(
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

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                onValueChange = { name = it },
                label = { Text(strings.mcp.nameLabel) },
                singleLine = true,
                isError = name.isBlank() || existingNames.contains(name),
            )

            GenericSelector(
                label = strings.mcp.transportLabel,
                selectedItem = transportType,
                onSelectItem = { transportType = it },
                options = McpTransportType.entries,
                itemLabeler = { it.name },
                modifier = Modifier.widthIn(min = 180.dp).weight(0.7f),
            )

            FilledTonalIconButton(
                modifier = Modifier.offset(y = 4.dp),
                onClick = {
                    val transportSpecification = when (transportType) {
                        McpTransportType.Stdio -> TransportSpecification.Stdio
                        McpTransportType.Sse -> TransportSpecification.Sse(
                            url = sseUrl, reconnectionTime = sseReconnectionTimeText.toLongOrNull()?.seconds
                        )
                    }

                    val process = if (addProcess) {
                        val cmd = commandText.parseCommandString()

                        ProcessSpecification(
                            command = cmd,
                            environment = environmentText.parseEnvironmentString().ifEmpty { null },
                        )
                    } else null

                    val serverSpecification = ServerSpecification(
                        transport = transportSpecification, process = process
                    )
                    onAdd(name, serverSpecification)

                    // Reset
                    name = ""
                    transportType = McpTransportType.Stdio
                    sseUrl = ""
                    sseReconnectionTimeText = ""
                    addProcess = false
                    commandText = ""
                    environmentText = ""
                },
                enabled = isValid(),
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = strings.mcp.addServerAria)
            }
        }

        if (transportType == McpTransportType.Sse) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = sseUrl,
                onValueChange = { sseUrl = it },
                label = { Text(strings.mcp.sseUrlLabel) },
                singleLine = true,
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = sseReconnectionTimeText,
                onValueChange = { sseReconnectionTimeText = it.filter { ch -> ch.isDigit() } },
                label = { Text(strings.mcp.reconnectionTimeLabel) },
                singleLine = true,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = strings.mcp.runAsProcessLabel,
            )
            Switch(checked = addProcess, onCheckedChange = { addProcess = it })
        }

        if (addProcess) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = commandText,
                onValueChange = { commandText = it },
                label = { Text(strings.mcp.commandLabel) },
                singleLine = true,
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = environmentText,
                onValueChange = { environmentText = it },
                label = { Text(strings.mcp.environmentLabel) },
                singleLine = true,
            )
        }
    }
}

private const val COMMAND_ELEMENTS_SEPARATOR = " "

private fun List<String>.buildCommandString(): String = joinToString(COMMAND_ELEMENTS_SEPARATOR)

private fun String.parseCommandString(): List<String> =
    split(COMMAND_ELEMENTS_SEPARATOR).filter { it.isNotBlank() }

private const val ENVIRONMENT_ELEMENTS_SEPARATOR = ";"

private fun Map<String, String>.buildEnvironmentString(): String =
    entries.joinToString("$ENVIRONMENT_ELEMENTS_SEPARATOR ") { "${it.key}=${it.value}" }

private fun String.parseEnvironmentString(): Map<String, String> {
    if (isBlank()) return emptyMap()
    return split(ENVIRONMENT_ELEMENTS_SEPARATOR).mapNotNull { entry ->
        val trimmed = entry.trim()
        if (trimmed.isBlank()) return@mapNotNull null
        val index = trimmed.indexOf('=')
        if (index <= 0 || index >= trimmed.lastIndex) return@mapNotNull null
        val key = trimmed.take(index).trim()
        val value = trimmed.substring(index + 1).trim()
        if (key.isEmpty()) null else key to value
    }.toMap()
}

private enum class McpTransportType { Stdio, Sse }

private sealed interface McpServersSheetState {
    data object Closed : McpServersSheetState
    data object Adding : McpServersSheetState
    data class Editing(val name: String) : McpServersSheetState
}

private fun TransportSpecification.toType(): McpTransportType = when (this) {
    TransportSpecification.Stdio -> McpTransportType.Stdio
    is TransportSpecification.Sse -> McpTransportType.Sse
}
