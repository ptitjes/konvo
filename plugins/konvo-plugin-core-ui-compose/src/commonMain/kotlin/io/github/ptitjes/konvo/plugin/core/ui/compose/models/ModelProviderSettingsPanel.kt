package io.github.ptitjes.konvo.plugin.core.ui.compose.models

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import com.slack.circuit.runtime.screen.Screen
import io.github.ptitjes.konvo.plugin.core.models.*
import io.github.ptitjes.konvo.plugin.core.models.ModelProviderConfiguration.*
import io.github.ptitjes.konvo.plugin.core.models.providers.*
import io.github.ptitjes.konvo.plugin.core.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.utils.*
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.*
import org.kodein.di.compose.*
import sh.calvin.reorderable.*

internal class ModelProviderSettingsPresenter(
    private val settingsRepository: SettingsRepository,
) : Presenter<ModelProviderSettingsView.State> {
    @Composable
    override fun present(): ModelProviderSettingsView.State {
        var settings by settingsRepository.mutableSettingsOf(ModelProviderSettingsKey)

        return ModelProviderSettingsView.State(settings) { event ->
            when (event) {
                is ModelProviderSettingsView.Event.UpdateSettings -> {
                    settings = event.settings
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanelScope.ModelProviderSettingsPanel(state: ModelProviderSettingsView.State) {
    val settings = state.settings
    val modelManager by rememberInstance<SettingsBasedModelManager>()

    val providerStatuses by modelManager.providerStatuses.collectAsState()

    fun addProvider(newProvider: NamedModelProvider) {
        state.eventSink(
            ModelProviderSettingsView.Event.UpdateSettings(
                settings.copy(providers = settings.providers + newProvider)
            )
        )
    }

    fun updateProvider(index: Int, transform: (previous: NamedModelProvider) -> NamedModelProvider) {
        state.eventSink(
            ModelProviderSettingsView.Event.UpdateSettings(
                settings.copy(
                    providers = settings.providers.mapIndexed { i, provider ->
                        if (i == index) transform(provider) else provider
                    }
                )
            )
        )
    }

    fun removeProvider(index: Int) {
        state.eventSink(
            ModelProviderSettingsView.Event.UpdateSettings(
                settings.copy(providers = settings.providers.filterIndexed { i, _ -> i != index })
            )
        )
    }

    fun moveProvider(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        state.eventSink(
            ModelProviderSettingsView.Event.UpdateSettings(
                settings.copy(
                    providers = settings.providers.mutate {
                        val provider = removeAt(fromIndex)
                        val targetIndex = toIndex.coerceIn(0, size)
                        add(targetIndex, provider)
                    }
                )
            )
        )
    }

    var sheetState by remember { mutableStateOf<ModelProvidersSheetState>(ModelProvidersSheetState.Closed) }
    var providerPendingDeletionIndex by remember { mutableStateOf<Int?>(null) }

    SettingsBox(
        title = i18n.models.configuredProvidersTitle,
        description = i18n.models.configuredProvidersDescription,
        trailingContent = {
            FilledTonalIconButton(
                onClick = { sheetState = ModelProvidersSheetState.Adding },
            ) {
                Icon(painter = painterResource(Res.drawable.ic_add), contentDescription = i18n.models.addProviderAria)
            }
        },
        bottomContent = {
            if (settings.providers.isEmpty()) {
                Text(
                    text = i18n.models.noProvidersMessage,
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
                                            painter = painterResource(Res.drawable.ic_drag_handle),
                                            contentDescription = i18n.models.dragHandleAria,
                                        )
                                    }

                                    ModelProviderIcons.iconFor(provider.configuration)?.let { icon ->
                                        Icon(
                                            painter = painterResource(icon),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = provider.name,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = provider.configuration.toType().name,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                            Spacer(Modifier.width(8.dp))

                                            Box(
                                                modifier = Modifier.size(16.dp).clickable {
                                                    modelManager.reloadModelProvider(provider.name)
                                                },
                                            ) {
                                                when (val status = providerStatuses[provider.name]) {
                                                    is ModelProviderStatus.Pending -> {
                                                        CircularProgressIndicator(
                                                            strokeWidth = 2.dp
                                                        )
                                                    }

                                                    is ModelProviderStatus.Available -> {
                                                        Icon(
                                                            painter = painterResource(Res.drawable.ic_check),
                                                            contentDescription = i18n.models.testProviderSuccessAria,
                                                            tint = Color.Green,
                                                        )
                                                    }

                                                    is ModelProviderStatus.Unavailable -> {
                                                        Icon(
                                                            painter = painterResource(Res.drawable.ic_error),
                                                            contentDescription = i18n.models.testProviderFailureAria,
                                                            tint = Color.Red,
                                                        )
                                                    }

                                                    null -> {
                                                        Icon(
                                                            painter = painterResource(Res.drawable.ic_play),
                                                            contentDescription = i18n.models.testProviderAria,
                                                            tint = Color.Gray,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = { sheetState = ModelProvidersSheetState.Editing(index) },
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_edit),
                                            contentDescription = i18n.models.editProviderAria,
                                        )
                                    }

                                    IconButton(
                                        onClick = { providerPendingDeletionIndex = index },
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_delete),
                                            contentDescription = i18n.models.deleteProviderAria,
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
        val nameToDelete =
            settings.providers.getOrNull(indexToDelete)?.name ?: i18n.models.nameLabel.lowercase()
        AlertDialog(
            onDismissRequest = { providerPendingDeletionIndex = null },
            title = { Text(i18n.models.deleteProviderDialogTitle) },
            text = { Text(i18n.models.deleteProviderDialogText(nameToDelete)) },
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
                    Text(i18n.models.deleteConfirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { providerPendingDeletionIndex = null }) { Text(i18n.models.cancel) }
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
                    val provider = settings.providers[sheet.index]
                    val status = providerStatuses[provider.name]

                    EditProviderSheetContent(
                        initialProvider = provider,
                        initialTestResult = status?.toTestResult() ?: TestResult.Unknown,
                        existingNames = settings.providers.map { it.name }
                            .toSet() - settings.providers[index].name,
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
    initialTestResult: TestResult,
    existingNames: Set<String>,
    onSave: (NamedModelProvider) -> Unit,
    onDelete: () -> Unit,
) {
    var provider by remember { mutableStateOf(initialProvider) }
    var testResult by remember { mutableStateOf(initialTestResult) }

    fun updateDraft(update: (NamedModelProvider) -> NamedModelProvider) {
        provider = update(provider)
        testResult = TestResult.Unknown
    }

    ModelProviderSheetLayout(
        sheetType = SheetType.Edit,
        provider = provider,
        onNameChange = { newName -> updateDraft { it.copy(name = newName) } },
        onTypeChange = { newType ->
            if (provider.configuration.toType() != newType) {
                updateDraft { it.copy(configuration = newType.newConfiguration()) }
            }
        },
        configurationForm = {
            when (val conf = provider.configuration) {
                is Ollama -> {
                    OutlinedUrlField(
                        modifier = Modifier.fillMaxWidth(),
                        value = conf.url,
                        onValueChange = { newUrl ->
                            updateDraft { it.copy(configuration = conf.copy(url = newUrl)) }
                        },
                        label = { Text(i18n.models.ollamaBaseUrlLabel) },
                    )
                }

                is Anthropic -> {
                    OutlinedApiKeyField(
                        modifier = Modifier.fillMaxWidth(),
                        value = conf.apiKey,
                        onValueChange = { newKey ->
                            updateDraft { it.copy(configuration = conf.copy(apiKey = newKey)) }
                        },
                        label = { Text(i18n.models.anthropicApiKeyLabel) },
                    )
                }

                is OpenAI -> {
                    OutlinedApiKeyField(
                        modifier = Modifier.fillMaxWidth(),
                        value = conf.apiKey,
                        onValueChange = { newKey ->
                            updateDraft { it.copy(configuration = conf.copy(apiKey = newKey)) }
                        },
                        label = { Text(i18n.models.openAiApiKeyLabel) },
                    )
                }

                is Google -> {
                    OutlinedApiKeyField(
                        modifier = Modifier.fillMaxWidth(),
                        value = conf.apiKey,
                        onValueChange = { newKey ->
                            updateDraft { it.copy(configuration = conf.copy(apiKey = newKey)) }
                        },
                        label = { Text(i18n.models.googleApiKeyLabel) },
                    )
                }

                is MistralAI -> {
                    OutlinedApiKeyField(
                        modifier = Modifier.fillMaxWidth(),
                        value = conf.apiKey,
                        onValueChange = { newKey ->
                            updateDraft { it.copy(configuration = conf.copy(apiKey = newKey)) }
                        },
                        label = { Text(i18n.models.mistralAiApiKeyLabel) },
                    )
                }
            }
        },
        testResult = testResult,
        onTestResultChange = { testResult = it },
        uniqueProviderNames = existingNames,
        onDelete = { onDelete() },
        onAddSave = { onSave(provider) },
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
    var mistralAIKey by remember { mutableStateOf("") }

    val provider = remember(name, type, ollamaUrl, anthropicKey, openAIKey, googleKey, mistralAIKey) {
        NamedModelProvider(
            name = name,
            configuration = when (type) {
                ProviderType.Ollama -> Ollama(url = ollamaUrl)
                ProviderType.Anthropic -> Anthropic(apiKey = anthropicKey)
                ProviderType.OpenAI -> OpenAI(apiKey = openAIKey)
                ProviderType.Google -> Google(apiKey = googleKey)
                ProviderType.MistralAI -> MistralAI(apiKey = mistralAIKey)
            },
        )
    }

    var testResult by remember(provider) { mutableStateOf<TestResult>(TestResult.Unknown) }

    ModelProviderSheetLayout(
        sheetType = SheetType.Add,
        provider = provider,
        onNameChange = { name = it },
        onTypeChange = { type = it },
        configurationForm = {
            when (type) {
                ProviderType.Ollama -> {
                    OutlinedUrlField(
                        value = ollamaUrl,
                        onValueChange = { ollamaUrl = it },
                        label = { Text(i18n.models.ollamaBaseUrlLabel) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ProviderType.Anthropic -> {
                    OutlinedApiKeyField(
                        value = anthropicKey,
                        onValueChange = { anthropicKey = it },
                        label = { Text(i18n.models.anthropicApiKeyLabel) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ProviderType.OpenAI -> {
                    OutlinedApiKeyField(
                        value = openAIKey,
                        onValueChange = { openAIKey = it },
                        label = { Text(i18n.models.openAiApiKeyLabel) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ProviderType.Google -> {
                    OutlinedApiKeyField(
                        value = googleKey,
                        onValueChange = { googleKey = it },
                        label = { Text(i18n.models.googleApiKeyLabel) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ProviderType.MistralAI -> {
                    OutlinedApiKeyField(
                        value = mistralAIKey,
                        onValueChange = { mistralAIKey = it },
                        label = { Text(i18n.models.mistralAiApiKeyLabel) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        testResult = testResult,
        onTestResultChange = { testResult = it },
        uniqueProviderNames = existingNames,
        onAddSave = { onAdd(provider) },
    )
}

private fun ModelProviderConfiguration.toType(): ProviderType = when (this) {
    is Ollama -> ProviderType.Ollama
    is Anthropic -> ProviderType.Anthropic
    is OpenAI -> ProviderType.OpenAI
    is Google -> ProviderType.Google
    is MistralAI -> ProviderType.MistralAI
}

private fun ModelProviderConfiguration.isValid(): Boolean = when (this) {
    is Ollama -> this.url.isNotBlank()
    is Anthropic -> this.apiKey.isNotBlank()
    is OpenAI -> this.apiKey.isNotBlank()
    is Google -> this.apiKey.isNotBlank()
    is MistralAI -> this.apiKey.isNotBlank()
}

private fun ProviderType.newConfiguration(): ModelProviderConfiguration {
    return when (this) {
        ProviderType.Ollama -> Ollama(url = DEFAULT_OLLAMA_URL)
        ProviderType.Anthropic -> Anthropic(apiKey = "")
        ProviderType.OpenAI -> OpenAI(apiKey = "")
        ProviderType.Google -> Google(apiKey = "")
        ProviderType.MistralAI -> MistralAI(apiKey = "")
    }
}

private fun NamedModelProvider.isValid(): Boolean = name.isNotBlank() && configuration.isValid()

private enum class SheetType { Add, Edit }

@Composable
private fun ModelProviderSheetLayout(
    sheetType: SheetType,
    provider: NamedModelProvider,
    onNameChange: (String) -> Unit,
    onTypeChange: (ProviderType) -> Unit,
    configurationForm: @Composable () -> Unit,
    testResult: TestResult,
    onTestResultChange: (TestResult) -> Unit,
    uniqueProviderNames: Set<String>,
    onDelete: () -> Unit = {},
    onAddSave: () -> Unit,
) {
    val name = provider.name
    val type = provider.configuration.toType()

    val runTest = rememberProviderTester(provider) {
        onTestResultChange(it)
    }

    val nameErrorText = when {
        name.isBlank() -> i18n.models.nameEmptyError
        uniqueProviderNames.contains(name) -> i18n.models.nameUniqueError
        else -> null
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

        configurationForm()

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
            TestButton(result = testResult, onClick = runTest)
            if (sheetType == SheetType.Edit) DeleteButton(onClick = onDelete)

            Spacer(Modifier.weight(1f))

            AddSaveButton(
                actionType = when (sheetType) {
                    SheetType.Edit -> AddSaveActionType.Save
                    SheetType.Add -> AddSaveActionType.Add
                },
                onClick = { onAddSave() },
                enabled = provider.isValid() && provider.name !in uniqueProviderNames,
            )
        }

        when (testResult) {
            is TestResult.Failure -> {
                Text(
                    text = i18n.models.testFailedMessage(testResult.error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            else -> {}
        }
    }
}

@Composable
private fun rememberProviderTester(
    provider: NamedModelProvider,
    onTestResultChange: (TestResult) -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()

    return remember(provider) {
        {
            onTestResultChange(TestResult.Pending)
            scope.launch {
                provider.test()
                    .onSuccess { onTestResultChange(TestResult.Success) }
                    .onFailure { throwable ->
                        onTestResultChange(TestResult.Failure(throwable.message ?: "Unknown error"))
                    }
            }
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
        label = { Text(i18n.models.nameLabel) },
        singleLine = true,
        isError = isError,
    )
}

private enum class ProviderType { Ollama, Anthropic, OpenAI, Google, MistralAI }

@Composable
private fun ModelProviderTypeSelector(
    selected: ProviderType,
    onSelected: (ProviderType) -> Unit,
    modifier: Modifier = Modifier,
) {
    GenericSelector(
        label = i18n.models.typeLabel,
        selectedItem = selected,
        onSelectItem = onSelected,
        options = ProviderType.entries,
        itemLabeler = { it.name },
        itemOption = { providerType ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ModelProviderIcons.iconFor(providerType.newConfiguration())?.let { icon ->
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(text = providerType.name)
            }
        },
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

private fun ModelProviderStatus.toTestResult() = when (this) {
    is ModelProviderStatus.Pending -> TestResult.Pending
    is ModelProviderStatus.Available -> TestResult.Success
    is ModelProviderStatus.Unavailable -> TestResult.Failure(this.reason)
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
                    Icon(
                        painter = painterResource(Res.drawable.ic_check),
                        contentDescription = i18n.models.testProviderSuccessAria,
                        tint = Color.Green,
                    )
                }

                is TestResult.Failure -> {
                    Icon(
                        painter = painterResource(Res.drawable.ic_error),
                        contentDescription = i18n.models.testProviderFailureAria,
                        tint = Color.Red,
                    )
                }

                is TestResult.Unknown -> {
                    Icon(
                        painter = painterResource(Res.drawable.ic_cached),
                        contentDescription = i18n.models.testProviderAria,
                    )
                }
            }
        },
        label = { Text(i18n.models.testAction) },
    )
}

@Composable
private fun DeleteButton(
    onClick: () -> Unit,
) {
    OutlinedActionButton(
        onClick = onClick,
        icon = { Icon(painter = painterResource(Res.drawable.ic_delete), contentDescription = i18n.models.deleteProviderAria) },
        label = { Text(i18n.models.deleteAction) },
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
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = i18n.models.addProviderConfirmAria
                )
            },
            label = { Text(i18n.models.addAction) },
        )

        AddSaveActionType.Save -> FilledActionButton(
            onClick = onClick,
            enabled = enabled,
            icon = { Icon(painter = painterResource(Res.drawable.ic_save), contentDescription = i18n.models.saveAction) },
            label = { Text(i18n.models.saveAction) },
        )
    }
}

data object ModelProviderSettingsView : Screen {
    data class State(
        val settings: ModelProviderSettings,
        val eventSink: (Event) -> Unit,
    ) : SettingsSectionState

    sealed interface Event : CircuitUiEvent {
        data class UpdateSettings(val settings: ModelProviderSettings) : Event
    }
}
