package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import com.slack.circuit.runtime.screen.Screen
import io.github.ptitjes.konvo.plugin.core.roleplay.*
import io.github.ptitjes.konvo.plugin.core.roleplay.providers.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*
import io.github.vinceglb.filekit.*
import io.github.vinceglb.filekit.dialogs.*
import io.github.vinceglb.filekit.dialogs.compose.*
import kotlinx.coroutines.*
import kotlinx.io.files.*
import org.jetbrains.compose.resources.*

internal class LorebooksSettingsPresenter(
    private val provider: FileSystemLorebookProvider,
) : Presenter<LorebooksSettingsView.State> {
    @Composable
    override fun present(): LorebooksSettingsView.State {
        var lorebooks by remember { mutableStateOf<List<Lorebook>?>(null) }
        var loadError by remember { mutableStateOf<String?>(null) }

        val scope = rememberCoroutineScope()

        suspend fun reload() {
            loadError = null
            runCatching { provider.query() }
                .onSuccess { list -> lorebooks = list.sortedBy { it.name ?: it.id ?: "" } }
                .onFailure { ex -> loadError = ex.message ?: "Unknown error" }
        }

        LaunchedEffect(Unit) { reload() }

        return when {
            loadError != null -> LorebooksSettingsView.State.Error(loadError!!)
            lorebooks == null -> LorebooksSettingsView.State.Loading
            else -> LorebooksSettingsView.State.Loaded(lorebooks!!) { event ->
                when (event) {
                    is LorebooksSettingsView.Event.Add -> scope.launch {
                        provider.add(event.path)
                        reload()
                    }

                    is LorebooksSettingsView.Event.Delete -> scope.launch {
                        provider.delete(event.lorebook)
                        reload()
                    }
                }
            }
        }
    }
}

@Composable
fun LorebooksSettingsPanel(state: LorebooksSettingsView.State) {
    val importLauncher = rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        type = FileKitType.File(extensions = listOf("json")),
    ) { files ->
        if (files.isNullOrEmpty()) return@rememberFilePickerLauncher
        if (state !is LorebooksSettingsView.State.Loaded) return@rememberFilePickerLauncher
        files.forEach { file ->
            state.eventSink(LorebooksSettingsView.Event.Add(file.toKotlinxIoPath()))
        }
    }

    var pendingDelete by remember { mutableStateOf<Lorebook?>(null) }

    SettingsBox(
        title = i18n.roleplay.importedLorebooksTitle,
        description = i18n.roleplay.importedLorebooksDescription,
        trailingContent = {
            FilledTonalIconButton(onClick = { importLauncher.launch() }) {
                Icon(
                    painter = painterResource(CoreResources.icons.download),
                    contentDescription = i18n.roleplay.importLorebookAria
                )
            }
        },
        bottomContent = {
            when (state) {
                is LorebooksSettingsView.State.Error -> Text(text = i18n.roleplay.failedToLoadLorebooks(state.error))
                is LorebooksSettingsView.State.Loading -> FullSizeProgressIndicator()
                is LorebooksSettingsView.State.Loaded if (state.lorebooks.isEmpty()) -> Text(text = i18n.roleplay.noLorebooksAvailable)
                is LorebooksSettingsView.State.Loaded -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.lorebooks.forEach { lorebook ->
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
                                    val title = lorebook.name ?: lorebook.id ?: i18n.roleplay.lorebookUnnamed
                                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                                    val description = lorebook.description
                                    if (!description.isNullOrBlank()) {
                                        Text(
                                            text = description,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                                IconButton(onClick = { pendingDelete = lorebook }) {
                                    Icon(
                                        painter = painterResource(CoreResources.icons.delete),
                                        contentDescription = i18n.roleplay.deleteLorebookAria
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )

    // Confirm deletion dialog
    pendingDelete?.let { toDelete ->
        if (state !is LorebooksSettingsView.State.Loaded) return@let
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(i18n.roleplay.deleteLorebookDialogTitle) },
            text = { Text(i18n.roleplay.deleteLorebookDialogText(toDelete.name ?: toDelete.id ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.eventSink(LorebooksSettingsView.Event.Delete(toDelete))
                        pendingDelete = null
                    },
                ) { Text(i18n.roleplay.deleteConfirm) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(i18n.roleplay.cancel) } },
        )
    }
}

data object LorebooksSettingsView : Screen {
    sealed interface State : SettingsSectionState {
        data object Loading : State
        data class Error(val error: String) : State
        data class Loaded(
            val lorebooks: List<Lorebook>,
            val eventSink: (Event) -> Unit,
        ) : State
    }

    sealed interface Event : CircuitUiEvent {
        data class Add(val path: Path) : Event
        data class Delete(val lorebook: Lorebook) : Event
    }
}
