package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import io.github.ptitjes.konvo.plugin.core.settings.*
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

internal data object CharacterSettingsView {
    data class State(
        val settings: CharacterSettings,
        val charactersState: CharactersState,
        val eventSink: (Event) -> Unit,
    ) : SettingsSectionState

    sealed interface CharactersState {
        data object Loading : CharactersState
        data class Loaded(
            val characters: List<CharacterCard>,
            val error: String?,
        ) : CharactersState
    }

    sealed interface Event : CircuitUiEvent {
        data class UpdateSettings(val settings: CharacterSettings) : Event
        data class AddCharacter(val path: Path) : Event
        data class DeleteCharacter(val character: CharacterCard) : Event
        data object AcknowledgeError : Event
    }
}

internal class CharacterSettingsPresenter(
    private val settingsRepository: SettingsRepository,
    private val characterManager: CharacterManager,
) : Presenter<CharacterSettingsView.State> {
    @Composable
    override fun present(): CharacterSettingsView.State {
        val characters by characterManager.characters.collectAsState(null)
        return state(characters)
    }

    @Composable
    private fun state(
        characters: Set<CharacterCard>?,
    ): CharacterSettingsView.State {
        var settings by settingsRepository.mutableSettingsOf(CharacterSettingsKey)
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            characterManager.error.collect { error = it }
        }

        val charactersState = when {
            characters == null -> CharacterSettingsView.CharactersState.Loading
            else -> CharacterSettingsView.CharactersState.Loaded(
                characters = characters.sortedBy { it.name },
                error = error,
            )
        }

        return CharacterSettingsView.State(settings, charactersState) { event ->
            when (event) {
                is CharacterSettingsView.Event.UpdateSettings -> settings = event.settings
                is CharacterSettingsView.Event.AddCharacter -> characterManager.add(event.path)
                is CharacterSettingsView.Event.DeleteCharacter -> characterManager.delete(event.character)
                is CharacterSettingsView.Event.AcknowledgeError -> error = null
            }
        }
    }
}

/**
 * Settings panel for character-related preferences.
 */
@Composable
internal fun CharacterSettingsPanel(state: CharacterSettingsView.State) {
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
                    val parsed = newValue.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                    state.eventSink(
                        CharacterSettingsView.Event.UpdateSettings(
                            settings.copy(filteredTags = parsed)
                        )
                    )
                },
                singleLine = true,
                placeholder = { Text(i18n.roleplay.characterTagsPlaceholder) },
            )
        },
    )

    ImportedCharactersSettingsBox(state)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportedCharactersSettingsBox(state: CharacterSettingsView.State) {
    val charactersState = state.charactersState

    val snackbarHostState = LocalSnackbarHost.current
    LaunchedEffect(charactersState) {
        if (charactersState is CharacterSettingsView.CharactersState.Loaded && charactersState.error != null) {
            snackbarHostState.showSnackbar(message = charactersState.error)
            state.eventSink(CharacterSettingsView.Event.AcknowledgeError)
        }
    }

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
        },
    )

    pendingDelete?.let { toDelete ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(i18n.roleplay.deleteCharacterDialogTitle) },
            text = { Text(i18n.roleplay.deleteCharacterDialogText(toDelete.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.eventSink(CharacterSettingsView.Event.DeleteCharacter(toDelete))
                        pendingDelete = null
                    }
                ) {
                    Text(i18n.roleplay.deleteConfirm)
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(i18n.roleplay.cancel) } },
        )
    }
}
