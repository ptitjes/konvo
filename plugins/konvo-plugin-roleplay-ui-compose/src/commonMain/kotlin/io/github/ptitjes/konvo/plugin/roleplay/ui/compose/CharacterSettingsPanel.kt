package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import com.slack.circuit.runtime.screen.Screen
import io.github.ptitjes.konvo.plugin.core.roleplay.*
import io.github.ptitjes.konvo.plugin.core.roleplay.providers.*
import io.github.ptitjes.konvo.plugin.core.settings.*
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

internal class CharacterSettingsPresenter(
    private val settingsRepository: SettingsRepository,
    private val provider: FileSystemCharacterProvider,
) : Presenter<CharacterSettingsView.State> {
    @Composable
    override fun present(): CharacterSettingsView.State {
        var settings by settingsRepository.mutableSettingsOf(CharacterSettingsKey)

        var characters by remember { mutableStateOf<List<CharacterCard>?>(null) }
        var loadError by remember { mutableStateOf<String?>(null) }

        val scope = rememberCoroutineScope()

        suspend fun reload() {
            loadError = null
            runCatching { provider.query() }
                .onSuccess { list -> characters = list.sortedBy { it.name } }
                .onFailure { ex ->
                    loadError = ex.message
                    characters = emptyList()
                }
        }

        LaunchedEffect(Unit) { reload() }

        val charactersState = when {
            loadError != null -> CharacterSettingsView.CharactersState.Error(loadError!!)
            characters == null -> CharacterSettingsView.CharactersState.Loading
            else -> CharacterSettingsView.CharactersState.Loaded(characters!!)
        }

        return CharacterSettingsView.State(settings, charactersState) { event ->
            when (event) {
                is CharacterSettingsView.Event.UpdateSettings -> {
                    settings = event.settings
                }

                is CharacterSettingsView.Event.AddCharacter -> scope.launch {
                    provider.add(event.path)
                    reload()
                }

                is CharacterSettingsView.Event.DeleteCharacter -> scope.launch {
                    provider.delete(event.character)
                    reload()
                }
            }
        }
    }
}

/**
 * Settings panel for character-related preferences.
 */
@Composable
fun SettingsPanelScope.CharacterSettingsPanel(state: CharacterSettingsView.State) {
    val settings = state.settings

    var filteredTagsText by remember(settings.filteredTags) {
        mutableStateOf(settings.filteredTags.joinToString(separator = ", "))
    }

    // Keep local text in sync if settings are externally updated
    LaunchedEffect(settings.filteredTags) {
        val joined = settings.filteredTags.joinToString(separator = ", ")
        if (joined != filteredTagsText) {
            filteredTagsText = joined
        }
    }

    SettingsBox(
        title = i18n.roleplay.characterTagsFilterTitle,
        description = i18n.roleplay.characterTagsFilterDescription,
        bottomContent = {
            OutlinedTextField(
                label = {},
                modifier = Modifier.height(64.dp).fillMaxWidth(),
                value = filteredTagsText,
                onValueChange = { newValue ->
                    filteredTagsText = newValue
                    val parsed = newValue.split(',')
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    state.eventSink(
                        CharacterSettingsView.Event.UpdateSettings(
                            settings.copy(filteredTags = parsed)
                        )
                    )
                },
                singleLine = true,
                placeholder = { Text(i18n.roleplay.characterTagsPlaceholder) },
            )
        }
    )

    ImportedCharactersSettingsBox(state)
}

data object CharacterSettingsView : Screen {
    data class State(
        val settings: CharacterSettings,
        val charactersState: CharactersState,
        val eventSink: (Event) -> Unit,
    ) : SettingsSectionState

    sealed interface CharactersState {
        data object Loading : CharactersState
        data class Error(val error: String) : CharactersState
        data class Loaded(val characters: List<CharacterCard>) : CharactersState
    }

    sealed interface Event : CircuitUiEvent {
        data class UpdateSettings(val settings: CharacterSettings) : Event
        data class AddCharacter(val path: Path) : Event
        data class DeleteCharacter(val character: CharacterCard) : Event
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsPanelScope.ImportedCharactersSettingsBox(state: CharacterSettingsView.State) {
    val charactersState = state.charactersState

    val importLauncher = rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        type = FileKitType.File(extensions = listOf("png", "json")),
    ) { files ->
        if (files.isNullOrEmpty()) return@rememberFilePickerLauncher
        files.forEach { file ->
            state.eventSink(CharacterSettingsView.Event.AddCharacter(file.toKotlinxIoPath()))
        }
    }

    var pendingDelete by remember { mutableStateOf<CharacterCard?>(null) }

    SettingsBox(
        title = i18n.roleplay.importedCharactersTitle,
        description = i18n.roleplay.importedCharactersDescription,
        trailingContent = {
            FilledTonalIconButton(onClick = { importLauncher.launch() }) {
                Icon(
                    painter = painterResource(CoreResources.icons.download),
                    contentDescription = i18n.roleplay.importCharactersAria
                )
            }
        },
        bottomContent = {
            when (charactersState) {
                is CharacterSettingsView.CharactersState.Error -> Text(
                    text = i18n.roleplay.failedToLoadCharacters(charactersState.error)
                )

                is CharacterSettingsView.CharactersState.Loading -> FullSizeProgressIndicator()
                is CharacterSettingsView.CharactersState.Loaded if (charactersState.characters.isEmpty()) ->
                    Text(text = i18n.roleplay.noCharactersAvailable)

                is CharacterSettingsView.CharactersState.Loaded -> CharacterGrid(
                    characters = charactersState.characters,
                    bottomEndContent = { character ->
                        FilledTonalIconButton(onClick = { pendingDelete = character }) {
                            Icon(
                                painter = painterResource(CoreResources.icons.delete),
                                contentDescription = i18n.roleplay.deleteCharacterAria,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                )
            }
        }
    )

    pendingDelete?.let { toDelete ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(i18n.roleplay.deleteCharacterDialogTitle) },
            text = { Text(i18n.roleplay.deleteCharacterDialogText(toDelete.name)) },
            confirmButton = {
                TextButton(onClick = {
                    state.eventSink(CharacterSettingsView.Event.DeleteCharacter(toDelete))
                    pendingDelete = null
                }) { Text(i18n.roleplay.deleteConfirm) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(i18n.roleplay.cancel) } },
        )
    }
}
