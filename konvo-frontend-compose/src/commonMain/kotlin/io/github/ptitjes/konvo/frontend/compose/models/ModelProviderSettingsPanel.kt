package io.github.ptitjes.konvo.frontend.compose.models

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.models.ModelProviderConfiguration.*
import io.github.ptitjes.konvo.core.models.providers.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import sh.calvin.reorderable.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelProviderSettingsPanel(
    settings: ModelProviderSettings,
    updateSettings: (updater: (previous: ModelProviderSettings) -> ModelProviderSettings) -> Unit,
) {
    fun addProvider(newProvider: NamedModelProvider) {
        updateSettings { previous -> previous.copy(providers = previous.providers + newProvider) }
    }

    fun updateProvider(index: Int, transform: (previous: NamedModelProvider) -> NamedModelProvider) {
        updateSettings { previous ->
            previous.copy(providers = previous.providers.mapIndexed { i, provider ->
                if (i == index) transform(provider) else provider
            })
        }
    }

    fun removeProvider(index: Int) {
        updateSettings { previous ->
            previous.copy(providers = previous.providers.filterIndexed { i, _ -> i != index })
        }
    }

    fun moveProvider(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        updateSettings { previous ->
            val list = previous.providers.toMutableList()
            val item = list.removeAt(fromIndex)
            val target = toIndex.coerceIn(0, list.size)
            list.add(target, item)
            previous.copy(providers = list)
        }
    }

    var sheetState by remember { mutableStateOf<ModelProvidersSheetState>(ModelProvidersSheetState.Closed) }
    var providerPendingDeletionIndex by remember { mutableStateOf<Int?>(null) }

    SettingsBox(
        title = "Configured providers",
        description = "Add, remove, and edit model providers.",
        trailingContent = {
            FilledTonalIconButton(
                onClick = { sheetState = ModelProvidersSheetState.Adding },
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add provider")
            }
        },
        bottomContent = {
            if (settings.providers.isEmpty()) {
                Text(
                    text = "No model providers configured.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                ReorderableColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    list = settings.providers,
                    onSettle = { fromIndex, toIndex -> moveProvider(fromIndex, toIndex) },
                ) { index, provider, _ ->
                    key(provider.name) {
                        ReorderableItem {
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
                                    IconButton(
                                        modifier = Modifier.draggableHandle(),
                                        onClick = { },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = "Drag handle",
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = provider.name,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Text(
                                            text = provider.configuration.toType().name,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }

                                    IconButton(
                                        onClick = { sheetState = ModelProvidersSheetState.Editing(index) },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit provider",
                                        )
                                    }

                                    IconButton(
                                        onClick = { providerPendingDeletionIndex = index },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete provider",
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )

    // Deletion confirmation dialog
    providerPendingDeletionIndex?.let { indexToDelete ->
        val nameToDelete = settings.providers.getOrNull(indexToDelete)?.name ?: "this provider"
        AlertDialog(
            onDismissRequest = { providerPendingDeletionIndex = null },
            title = { Text("Delete provider?") },
            text = { Text("Are you sure you want to delete \"$nameToDelete\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    // Confirm deletion
                    removeProvider(indexToDelete)
                    providerPendingDeletionIndex = null
                    val currentSheet = sheetState
                    if (currentSheet is ModelProvidersSheetState.Editing && currentSheet.index == indexToDelete) {
                        sheetState = ModelProvidersSheetState.Closed
                    }
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { providerPendingDeletionIndex = null }) { Text("Cancel") }
            },
        )
    }

    if (sheetState !is ModelProvidersSheetState.Closed) {
        ModalBottomSheet(
            onDismissRequest = { sheetState = ModelProvidersSheetState.Closed },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            when (val sheet = sheetState) {
                is ModelProvidersSheetState.Adding -> {
                    AddProviderSheetContent(
                        existingNames = settings.providers.map { it.name }.toSet(),
                        onAdd = { newProvider ->
                            addProvider(newProvider)
                            sheetState = ModelProvidersSheetState.Closed
                        },
                    )
                }

                is ModelProvidersSheetState.Editing -> {
                    val index = sheet.index
                    EditProviderSheetContent(
                        provider = settings.providers[sheet.index],
                        otherNames = settings.providers.map { it.name }.toSet() - settings.providers[index].name,
                        onChange = { updated -> updateProvider(index) { _ -> updated } },
                        onRemove = {
                            providerPendingDeletionIndex = index
                        },
                    )
                }

                is ModelProvidersSheetState.Closed -> {}
            }
        }
    }
}

@Composable
private fun EditProviderSheetContent(
    provider: NamedModelProvider,
    otherNames: Set<String>,
    onChange: (NamedModelProvider) -> Unit,
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
                value = provider.name,
                onValueChange = { newName -> onChange(provider.copy(name = newName)) },
                label = { Text("Name") },
                isError = provider.name.isBlank() || otherNames.contains(provider.name),
                singleLine = true,
            )

            var type by remember(provider.configuration) { mutableStateOf(provider.configuration.toType()) }
            GenericSelector(
                label = "Type",
                selectedItem = type,
                onSelectItem = { selected ->
                    type = selected
                    val newConfig = buildNewConfiguration(selected, provider)
                    onChange(provider.copy(configuration = newConfig))
                },
                options = ProviderType.entries,
                itemLabeler = { it.name },
                modifier = Modifier.widthIn(min = 180.dp).weight(0.7f),
            )

            IconButton(
                modifier = Modifier.offset(y = 4.dp),
                onClick = onRemove,
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove provider")
            }
        }

        when (val conf = provider.configuration) {
            is Ollama -> {
                OutlinedTextField(
                    value = conf.url,
                    onValueChange = { newUrl -> onChange(provider.copy(configuration = conf.copy(url = newUrl))) },
                    label = { Text("Ollama base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            is Anthropic -> {
                OutlinedTextField(
                    value = conf.apiKey,
                    onValueChange = { newKey -> onChange(provider.copy(configuration = conf.copy(apiKey = newKey))) },
                    label = { Text("Anthropic API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            is OpenAI -> {
                OutlinedTextField(
                    value = conf.apiKey,
                    onValueChange = { newKey -> onChange(provider.copy(configuration = conf.copy(apiKey = newKey))) },
                    label = { Text("OpenAI API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            is Google -> {
                OutlinedTextField(
                    value = conf.apiKey,
                    onValueChange = { newKey -> onChange(provider.copy(configuration = conf.copy(apiKey = newKey))) },
                    label = { Text("Google API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Optional helper text for name validity
        if (provider.name.isBlank()) {
            Text(
                text = "Name cannot be empty",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else if (otherNames.contains(provider.name)) {
            Text(
                text = "Name must be unique",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun buildNewConfiguration(
    selected: ProviderType,
    provider: NamedModelProvider,
): ModelProviderConfiguration = when (selected) {
    ProviderType.Ollama -> when (val configuration = provider.configuration) {
        is Ollama -> configuration
        else -> Ollama(url = DEFAULT_OLLAMA_URL)
    }

    ProviderType.Anthropic -> when (val configuration = provider.configuration) {
        is Anthropic -> configuration
        else -> Anthropic(apiKey = "")
    }

    ProviderType.OpenAI -> when (val configuration = provider.configuration) {
        is OpenAI -> configuration
        else -> OpenAI(apiKey = "")
    }

    ProviderType.Google -> when (val configuration = provider.configuration) {
        is Google -> configuration
        else -> Google(apiKey = "")
    }
}

@Composable
private fun AddProviderSheetContent(
    existingNames: Set<String>,
    onAdd: (NamedModelProvider) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ProviderType.Ollama) }
    var ollamaUrl by remember { mutableStateOf(DEFAULT_OLLAMA_URL) }
    var anthropicKey by remember { mutableStateOf("") }
    var openAIKey by remember { mutableStateOf("") }
    var googleKey by remember { mutableStateOf("") }

    fun isValid(): Boolean = name.isNotBlank() && !existingNames.contains(name) && when (type) {
        ProviderType.Ollama -> ollamaUrl.isNotBlank()
        ProviderType.Anthropic -> anthropicKey.isNotBlank()
        ProviderType.OpenAI -> openAIKey.isNotBlank()
        ProviderType.Google -> googleKey.isNotBlank()
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
                label = { Text("Name") },
                singleLine = true,
                isError = name.isBlank() || existingNames.contains(name),
            )

            GenericSelector(
                label = "Type",
                selectedItem = type,
                onSelectItem = { type = it },
                options = ProviderType.entries,
                itemLabeler = { it.name },
                modifier = Modifier.widthIn(min = 180.dp).weight(0.7f),
            )

            FilledTonalIconButton(
                modifier = Modifier.offset(y = 4.dp),
                onClick = {
                    val configuration: ModelProviderConfiguration = when (type) {
                        ProviderType.Ollama -> Ollama(url = ollamaUrl)
                        ProviderType.Anthropic -> Anthropic(apiKey = anthropicKey)
                        ProviderType.OpenAI -> OpenAI(apiKey = openAIKey)
                        ProviderType.Google -> Google(apiKey = googleKey)
                    }
                    onAdd(
                        NamedModelProvider(
                            name = name,
                            configuration = configuration,
                        )
                    )
                },
                enabled = isValid(),
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add provider")
            }
        }

        when (type) {
            ProviderType.Ollama -> {
                OutlinedTextField(
                    value = ollamaUrl,
                    onValueChange = { ollamaUrl = it },
                    label = { Text("Ollama base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            ProviderType.Anthropic -> {
                OutlinedTextField(
                    value = anthropicKey,
                    onValueChange = { anthropicKey = it },
                    label = { Text("Anthropic API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            ProviderType.OpenAI -> {
                OutlinedTextField(
                    value = openAIKey,
                    onValueChange = { openAIKey = it },
                    label = { Text("OpenAI API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            ProviderType.Google -> {
                OutlinedTextField(
                    value = googleKey,
                    onValueChange = { googleKey = it },
                    label = { Text("Google API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (name.isBlank()) {
            Text(
                text = "Name cannot be empty",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        } else if (existingNames.contains(name)) {
            Text(
                text = "Name must be unique",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private enum class ProviderType { Ollama, Anthropic, OpenAI, Google }

private sealed interface ModelProvidersSheetState {
    data object Closed : ModelProvidersSheetState
    data object Adding : ModelProvidersSheetState
    data class Editing(val index: Int) : ModelProvidersSheetState
}

private fun ModelProviderConfiguration.toType(): ProviderType = when (this) {
    is Ollama -> ProviderType.Ollama
    is Anthropic -> ProviderType.Anthropic
    is OpenAI -> ProviderType.OpenAI
    is Google -> ProviderType.Google
}
