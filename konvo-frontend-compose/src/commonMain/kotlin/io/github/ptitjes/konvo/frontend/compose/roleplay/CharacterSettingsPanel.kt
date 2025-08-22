package io.github.ptitjes.konvo.frontend.compose.roleplay

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.core.roleplay.providers.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.vinceglb.filekit.compose.*
import io.github.vinceglb.filekit.core.*
import kotlinx.coroutines.*
import org.kodein.di.compose.*

/**
 * Settings panel for character-related preferences.
 */
@Composable
fun CharacterSettingsPanel(
    settings: CharacterSettings,
    updateSettings: ((CharacterSettings) -> CharacterSettings) -> Unit,
) {
    var text by remember(settings.filteredTags) {
        mutableStateOf(settings.filteredTags.joinToString(separator = ", "))
    }

    // Keep local text in sync if settings are externally updated
    LaunchedEffect(settings.filteredTags) {
        val joined = settings.filteredTags.joinToString(separator = ", ")
        if (joined != text) {
            text = joined
        }
    }

    SettingsBox(
        title = "Character tags filter",
        description = "Tags listed here will be excluded when showing characters. Separate tags with commas.",
        bottomContent = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                value = text,
                onValueChange = { newValue ->
                    text = newValue
                    val parsed = newValue.split(',')
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    updateSettings { previous -> previous.copy(filteredTags = parsed) }
                },
                singleLine = true,
                placeholder = { Text("e.g. nsfw, beta, wip") },
            )
        }
    )

    ImportedCharactersSettingsBox()
}

private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportedCharactersSettingsBox() {
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

    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarHost(snackbarHostState)

    val importLauncher = rememberFilePickerLauncher(
        mode = PickerMode.Multiple(),
        type = PickerType.File(extensions = listOf("json", "png")),
    ) { files ->
        if (files.isNullOrEmpty()) return@rememberFilePickerLauncher
        scope.launch {
            files.forEach { file ->
                val result = runCatching { file.importCharacter(provider) }
                if (result.isFailure) {
                    logger.error(result.exceptionOrNull()) { "Failed to import file: $file" }
                    snackbarHostState.showSnackbar("Failed to import file: ${file.name}")
                } else {
                    reload()
                }
            }
        }
    }

    SettingsBox(
        title = "Imported characters",
        description = "Import, list and delete characters.",
        trailingContent = {
            FilledTonalIconButton(onClick = { importLauncher.launch() }) {
                Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Import characters")
            }
        },
        bottomContent = {
            when {
                loadError != null -> Text(text = "Failed to load characters: $loadError")
                characters == null -> FullSizeProgressIndicator()
                characters!!.isEmpty() -> Text(text = "No characters available.")
                else -> CharacterGrid(
                    characters = characters!!,
                    bottomEndContent = { character ->
                        FilledTonalIconButton(onClick = { pendingDelete = character }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete character",
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
            title = { Text("Delete character?") },
            text = { Text("Are you sure you want to delete \"${toDelete.name}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        runCatching { provider.delete(toDelete) }
                        pendingDelete = null
                        reload()
                    }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }
}

// Expect/actual helper to import a PlatformFile using the provider
expect suspend fun PlatformFile.importCharacter(provider: FileSystemCharacterProvider)
