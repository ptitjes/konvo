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
import io.github.ptitjes.konvo.frontend.compose.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import io.github.ptitjes.konvo.frontend.compose.utils.*
import kotlinx.coroutines.*
import sh.calvin.reorderable.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanelScope.ModelProviderSettingsPanel() {
    var settings by rememberMutableSettings(ModelProviderSettingsKey)

    fun addProvider(newProvider: NamedModelProvider) {
        settings = settings.copy(providers = settings.providers + newProvider)
    }

    fun updateProvider(index: Int, transform: (previous: NamedModelProvider) -> NamedModelProvider) {
        settings = settings.copy(
            providers = settings.providers.mapIndexed { i, provider ->
                if (i == index) transform(provider) else provider
            }
        )
    }

    fun removeProvider(index: Int) {
        settings = settings.copy(providers = settings.providers.filterIndexed { i, _ -> i != index })
    }

    fun moveProvider(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        settings = settings.copy(
            providers = settings.providers.mutate {
                val provider = removeAt(fromIndex)
                val targetIndex = toIndex.coerceIn(0, size)
                add(targetIndex, provider)
            }
        )
    }

    var sheetState by remember { mutableStateOf<ModelProvidersSheetState>(ModelProvidersSheetState.Closed) }
    var providerPendingDeletionIndex by remember { mutableStateOf<Int?>(null) }

    SettingsBox(
        title = strings.models.configuredProvidersTitle,
        description = strings.models.configuredProvidersDescription,
        trailingContent = {
            FilledTonalIconButton(
                onClick = { sheetState = ModelProvidersSheetState.Adding },
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = strings.models.addProviderAria)
            }
        },
        bottomContent = {
            if (settings.providers.isEmpty()) {
                Text(
                    text = strings.models.noProvidersMessage,
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
                                            contentDescription = strings.models.dragHandleAria,
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
                                            contentDescription = strings.models.editProviderAria,
                                        )
                                    }

                                    IconButton(
                                        onClick = { providerPendingDeletionIndex = index },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = strings.models.deleteProviderAria,
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
        val nameToDelete = settings.providers.getOrNull(indexToDelete)?.name ?: strings.models.nameLabel.lowercase()
        AlertDialog(
            onDismissRequest = { providerPendingDeletionIndex = null },
            title = { Text(strings.models.deleteProviderDialogTitle) },
            text = { Text(strings.models.deleteProviderDialogText(nameToDelete)) },
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
                    Text(strings.models.deleteConfirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { providerPendingDeletionIndex = null }) { Text(strings.models.cancel) }
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
                        initialProvider = settings.providers[sheet.index],
                        otherNames = settings.providers.map { it.name }.toSet() - settings.providers[index].name,
                        onSave = { updated ->
                            updateProvider(index) { _ -> updated }
                            sheetState = ModelProvidersSheetState.Closed
                        },
                        onDelete = {
                            providerPendingDeletionIndex = index
                        },
                    )
                }

                is ModelProvidersSheetState.Closed -> {}
            }
        }
    }
}

private sealed interface ModelProvidersSheetState {
    data object Closed : ModelProvidersSheetState
    data object Adding : ModelProvidersSheetState
    data class Editing(val index: Int) : ModelProvidersSheetState
}

@Composable
private fun EditProviderSheetContent(
    initialProvider: NamedModelProvider,
    otherNames: Set<String>,
    onSave: (NamedModelProvider) -> Unit,
    onDelete: () -> Unit,
) {
    var draft by remember { mutableStateOf(initialProvider) }
    var type by remember(draft.configuration) { mutableStateOf(draft.configuration.toType()) }

    ModelProviderSheetLayout(
        name = draft.name,
        onNameChange = { newName -> draft = draft.copy(name = newName) },
        type = type,
        onTypeChange = { newType ->
            if (type != newType) {
                type = newType
                draft = draft.copy(configuration = newType.newConfiguration())
            }
        },
        typeSpecificField = {
            when (val conf = draft.configuration) {
                is Ollama -> {
                    OutlinedUrlField(
                        modifier = Modifier.fillMaxWidth(),
                        value = conf.url,
                        onValueChange = { newUrl -> draft = draft.copy(configuration = conf.copy(url = newUrl)) },
                        label = { Text(strings.models.ollamaBaseUrlLabel) },
                    )
                }

                is Anthropic -> {
                    OutlinedApiKeyField(
                        modifier = Modifier.fillMaxWidth(),
                        value = conf.apiKey,
                        onValueChange = { newKey -> draft = draft.copy(configuration = conf.copy(apiKey = newKey)) },
                        label = { Text(strings.models.anthropicApiKeyLabel) },
                    )
                }

                is OpenAI -> {
                    OutlinedApiKeyField(
                        modifier = Modifier.fillMaxWidth(),
                        value = conf.apiKey,
                        onValueChange = { newKey -> draft = draft.copy(configuration = conf.copy(apiKey = newKey)) },
                        label = { Text(strings.models.openAiApiKeyLabel) },
                    )
                }

                is Google -> {
                    OutlinedApiKeyField(
                        modifier = Modifier.fillMaxWidth(),
                        value = conf.apiKey,
                        onValueChange = { newKey -> draft = draft.copy(configuration = conf.copy(apiKey = newKey)) },
                        label = { Text(strings.models.googleApiKeyLabel) },
                    )
                }
            }
        },
        uniqueNames = otherNames,
        buildProvider = { draft },
        startActions = { _, testResult, runTest ->
            TestButton(result = testResult, onClick = runTest)
            DeleteButton(onClick = onDelete)
        },
        endActions = { canSave, _, _ ->
            AddSaveButton(
                actionType = AddSaveActionType.Save,
                onClick = { onSave(draft) },
                enabled = canSave,
            )
        },
    )
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

    fun buildProvider(): NamedModelProvider = NamedModelProvider(
        name = name,
        configuration = when (type) {
            ProviderType.Ollama -> Ollama(url = ollamaUrl)
            ProviderType.Anthropic -> Anthropic(apiKey = anthropicKey)
            ProviderType.OpenAI -> OpenAI(apiKey = openAIKey)
            ProviderType.Google -> Google(apiKey = googleKey)
        },
    )

    ModelProviderSheetLayout(
        name = name,
        onNameChange = { name = it },
        type = type,
        onTypeChange = { type = it },
        typeSpecificField = {
            when (type) {
                ProviderType.Ollama -> {
                    OutlinedUrlField(
                        value = ollamaUrl,
                        onValueChange = { ollamaUrl = it },
                        label = { Text(strings.models.ollamaBaseUrlLabel) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ProviderType.Anthropic -> {
                    OutlinedApiKeyField(
                        value = anthropicKey,
                        onValueChange = { anthropicKey = it },
                        label = { Text(strings.models.anthropicApiKeyLabel) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ProviderType.OpenAI -> {
                    OutlinedApiKeyField(
                        value = openAIKey,
                        onValueChange = { openAIKey = it },
                        label = { Text(strings.models.openAiApiKeyLabel) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ProviderType.Google -> {
                    OutlinedApiKeyField(
                        value = googleKey,
                        onValueChange = { googleKey = it },
                        label = { Text(strings.models.googleApiKeyLabel) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        uniqueNames = existingNames,
        buildProvider = { buildProvider() },
        startActions = { _, testResult, runTest ->
            TestButton(result = testResult, onClick = runTest)
        },
        endActions = { canSave, _, _ ->
            AddSaveButton(
                actionType = AddSaveActionType.Add,
                onClick = { onAdd(buildProvider()) },
                enabled = canSave,
            )
        },
    )
}

private fun ModelProviderConfiguration.toType(): ProviderType = when (this) {
    is Ollama -> ProviderType.Ollama
    is Anthropic -> ProviderType.Anthropic
    is OpenAI -> ProviderType.OpenAI
    is Google -> ProviderType.Google
}

private fun ModelProviderConfiguration.isValid(): Boolean = when (this) {
    is Ollama -> this.url.isNotBlank()
    is Anthropic -> this.apiKey.isNotBlank()
    is OpenAI -> this.apiKey.isNotBlank()
    is Google -> this.apiKey.isNotBlank()
}

private fun ProviderType.newConfiguration(): ModelProviderConfiguration {
    return when (this) {
        ProviderType.Ollama -> Ollama(url = DEFAULT_OLLAMA_URL)
        ProviderType.Anthropic -> Anthropic(apiKey = "")
        ProviderType.OpenAI -> OpenAI(apiKey = "")
        ProviderType.Google -> Google(apiKey = "")
    }
}

private fun NamedModelProvider.isValid(): Boolean = name.isNotBlank() && configuration.isValid()

@Composable
private fun ModelProviderSheetLayout(
    name: String,
    onNameChange: (String) -> Unit,
    type: ProviderType,
    onTypeChange: (ProviderType) -> Unit,
    typeSpecificField: @Composable () -> Unit,
    uniqueNames: Set<String>,
    buildProvider: () -> NamedModelProvider,
    startActions: @Composable RowScope.(canSave: Boolean, testResult: TestResult, runTest: () -> Unit) -> Unit,
    endActions: @Composable RowScope.(canSave: Boolean, testResult: TestResult, runTest: () -> Unit) -> Unit,
) {
    var testResult by remember { mutableStateOf<TestResult>(TestResult.Unknown) }
    val scope = rememberCoroutineScope()

    val nameErrorText = when {
        name.isBlank() -> strings.models.nameEmptyError
        uniqueNames.contains(name) -> strings.models.nameUniqueError
        else -> null
    }

    fun runTest() {
        testResult = TestResult.Pending
        val provider = buildProvider()
        scope.launch {
            provider.test()
                .onSuccess { testResult = TestResult.Success }
                .onFailure { throwable ->
                    testResult = TestResult.Failure(throwable.message ?: "Unknown error")
                }
        }
    }

    val canSave = (nameErrorText == null) && buildProvider().isValid()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedNameField(
                modifier = Modifier.weight(1f),
                value = name,
                onValueChange = onNameChange,
                isError = nameErrorText != null,
            )

            ModelProviderTypeSelector(
                modifier = Modifier.widthIn(min = 180.dp).weight(0.7f),
                selected = type,
                onSelected = onTypeChange,
            )
        }

        typeSpecificField()

        nameErrorText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            startActions(canSave, testResult, ::runTest)
            Spacer(Modifier.weight(1f))
            endActions(canSave, testResult, ::runTest)
        }

        when (val result = testResult) {
            is TestResult.Failure -> {
                Text(
                    text = strings.models.testFailedMessage(result.error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            else -> {}
        }
    }
}

@Composable
private fun OutlinedNameField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        modifier = modifier.height(64.dp),
        value = value,
        onValueChange = onValueChange,
        label = { Text(strings.models.nameLabel) },
        singleLine = true,
        isError = isError,
    )
}

private enum class ProviderType { Ollama, Anthropic, OpenAI, Google }

@Composable
private fun ModelProviderTypeSelector(
    selected: ProviderType,
    onSelected: (ProviderType) -> Unit,
    modifier: Modifier = Modifier,
) {
    GenericSelector(
        label = strings.models.typeLabel,
        selectedItem = selected,
        onSelectItem = onSelected,
        options = ProviderType.entries,
        itemLabeler = { it.name },
        modifier = modifier,
    )
}

@Composable
private fun OutlinedUrlField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        modifier = modifier.height(64.dp),
        value = value,
        onValueChange = onValueChange,
        label = label,
        singleLine = true,
    )
}

@Composable
private fun OutlinedApiKeyField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        modifier = modifier.height(64.dp),
        value = value,
        onValueChange = onValueChange,
        label = label,
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
    )
}

private sealed class TestResult {
    data object Unknown : TestResult()
    data object Pending : TestResult()
    data object Success : TestResult()
    data class Failure(val error: String) : TestResult()
}

@Composable
private fun TestButton(
    result: TestResult,
    onClick: () -> Unit,
) {
    FilledTonalActionButton(
        onClick = onClick,
        enabled = result !is TestResult.Pending,
        content = {
            when (result) {
                is TestResult.Pending -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }

                is TestResult.Success -> {
                    Icon(imageVector = Icons.Default.Check, contentDescription = strings.models.testProviderSuccessAria)
                }

                is TestResult.Failure -> {
                    Icon(imageVector = Icons.Default.Error, contentDescription = strings.models.testProviderFailureAria)
                }

                is TestResult.Unknown -> {
                    Icon(imageVector = Icons.Default.Try, contentDescription = strings.models.testProviderAria)
                }
            }
        },
        label = { Text(strings.models.testAction) },
    )
}

@Composable
private fun DeleteButton(
    onClick: () -> Unit,
) {
    OutlinedActionButton(
        onClick = onClick,
        icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = strings.models.deleteProviderAria) },
        label = { Text(strings.models.deleteAction) },
    )
}

private enum class AddSaveActionType { Add, Save }

@Composable
private fun AddSaveButton(
    actionType: AddSaveActionType,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    when (actionType) {
        AddSaveActionType.Add -> FilledActionButton(
            onClick = onClick,
            enabled = enabled,
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = strings.models.addProviderConfirmAria
                )
            },
            label = { Text(strings.models.addAction) },
        )

        AddSaveActionType.Save -> FilledActionButton(
            onClick = onClick,
            enabled = enabled,
            icon = { Icon(imageVector = Icons.Default.Save, contentDescription = strings.models.saveAction) },
            label = { Text(strings.models.saveAction) },
        )
    }
}
