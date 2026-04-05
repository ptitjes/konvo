package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.overlays.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.plugin.roleplay.*
import io.github.vinceglb.filekit.*
import io.github.vinceglb.filekit.dialogs.*
import io.github.vinceglb.filekit.dialogs.compose.*
import kotlinx.io.files.*
import org.jetbrains.compose.resources.*

internal data object LorebooksSettingsView {
    data class State(
        val lorebooksState: LorebooksState,
        val eventSink: (Event) -> Unit,
    ) : SettingsSectionState

    sealed interface LorebooksState {
        data object Loading : LorebooksState
        data class Loaded(
            val lorebooks: List<Lorebook>,
            val error: String?,
        ) : LorebooksState
    }

    sealed interface Event : CircuitUiEvent {
        data class Add(val path: Path) : Event
        data class Delete(val lorebook: Lorebook) : Event
        data object AcknowledgeError : Event
    }
}

internal class LorebooksSettingsPresenter(
    private val lorebookManager: LorebookManager,
) : Presenter<LorebooksSettingsView.State> {
    @Composable
    override fun present(): LorebooksSettingsView.State {
        val lorebooks by lorebookManager.lorebooks.collectAsState(null)
        return state(lorebooks)
    }

    @Composable
    private fun state(lorebooks: Set<Lorebook>?): LorebooksSettingsView.State {
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            lorebookManager.error.collect { error = it }
        }

        val lorebooksState = when {
            lorebooks == null -> LorebooksSettingsView.LorebooksState.Loading
            else -> LorebooksSettingsView.LorebooksState.Loaded(
                lorebooks = lorebooks.sortedBy { it.name },
                error = error,
            )
        }

        return LorebooksSettingsView.State(lorebooksState) { event ->
            when (event) {
                is LorebooksSettingsView.Event.Add -> lorebookManager.add(event.path)
                is LorebooksSettingsView.Event.Delete -> lorebookManager.delete(event.lorebook)
                is LorebooksSettingsView.Event.AcknowledgeError -> error = null
            }
        }
    }
}

@Composable
internal fun LorebooksSettingsPanel(state: LorebooksSettingsView.State) {
    val lorebooksState = state.lorebooksState

    val snackbarHostState = LocalSnackbarHost.current
    LaunchedEffect(lorebooksState) {
        if (lorebooksState is LorebooksSettingsView.LorebooksState.Loaded && lorebooksState.error != null) {
            snackbarHostState.showSnackbar(message = lorebooksState.error)
            state.eventSink(LorebooksSettingsView.Event.AcknowledgeError)
        }
    }

    val importLauncher = rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        type = FileKitType.File(extensions = listOf("json")),
    ) { files ->
        if (files.isNullOrEmpty()) return@rememberFilePickerLauncher
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
            when (lorebooksState) {
                is LorebooksSettingsView.LorebooksState.Loading -> FullSizeProgressIndicator()
                is LorebooksSettingsView.LorebooksState.Loaded if (lorebooksState.lorebooks.isEmpty()) ->
                    Text(text = i18n.roleplay.noLorebooksAvailable)

                is LorebooksSettingsView.LorebooksState.Loaded -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    lorebooksState.lorebooks.forEach { lorebook ->
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
        },
    )

    // Confirm deletion dialog
    pendingDelete?.let { toDelete ->
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
