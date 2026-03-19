package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.oshai.kotlinlogging.*
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
import org.jetbrains.compose.resources.*
import org.kodein.di.compose.*

/**
 * Settings panel for character-related preferences.
 */
@Composable
fun SettingsPanelScope.CharacterSettingsPanel() {
    var settings by rememberMutableSettings(CharacterSettingsKey)

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
                    settings = settings.copy(filteredTags = parsed)
                },
                singleLine = true,
                placeholder = { Text(i18n.roleplay.characterTagsPlaceholder) },
            )
        }
    )

    ImportedCharactersSettingsBox()
}

private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsPanelScope.ImportedCharactersSettingsBox() {
    val provider by rememberInstance<FileSystemCharacterProvider>()
    val scope = rememberCoroutineScope()

    var characters by remember { mutableStateOf<List<CharacterCard>?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<CharacterCard?>(null) }

    fun reload() {
        loadError = null
        scope.launch {
            runCatching { provider.query() }
                .onSuccess { list -> characters = list.sortedBy { it.name } }
                .onFailure { ex ->
                    loadError = ex.message
                    characters = emptyList()
                }
        }
    }

    LaunchedEffect(Unit) { reload() }

    val importLauncher = rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        type = FileKitType.File(extensions = listOf("png", "json")),
    ) { files ->
        if (files.isNullOrEmpty()) return@rememberFilePickerLauncher
        scope.launch {
            files.forEach { file ->
                runCatching { provider.add(file.toKotlinxIoPath()) }
                    .onSuccess { reload() }
                    .onFailure { exception ->
                        logger.error(exception) { "Failed to import character" }
                        showSnackbar("Failed to import character:\n${exception.message ?: "Unknown error"}")
                    }
            }
        }
    }

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
            when {
                loadError != null -> Text(text = i18n.roleplay.failedToLoadCharacters(loadError!!))
                characters == null -> FullSizeProgressIndicator()
                characters!!.isEmpty() -> Text(text = i18n.roleplay.noCharactersAvailable)
                else -> CharacterGrid(
                    characters = characters!!,
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
                    scope.launch {
                        pendingDelete = null
                        runCatching { provider.delete(toDelete) }
                            .onSuccess { reload() }
                            .onFailure { exception ->
                                logger.error(exception) { "Failed to delete character" }
                                showSnackbar("Failed to delete character:\n${exception.message ?: "Unknown error"}")
                            }
                    }
                }) { Text(i18n.roleplay.deleteConfirm) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(i18n.roleplay.cancel) } },
        )
    }
}
