package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.core.roleplay.providers.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import io.github.ptitjes.konvo.frontend.compose.components.settings.*
import io.github.vinceglb.filekit.compose.*
import io.github.vinceglb.filekit.core.*
import kotlinx.coroutines.*
import org.kodein.di.compose.*

private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LorebooksSettingsPanel() {
    val provider by rememberInstance<FileSystemLorebookProvider>()
    val scope = rememberCoroutineScope()

    var lorebooks by remember { mutableStateOf<List<Lorebook>?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<Lorebook?>(null) }

    fun reload() {
        loadError = null
        scope.launch {
            runCatching { provider.query() }
                .onSuccess { list -> lorebooks = list.sortedBy { it.name ?: it.id ?: "" } }
                .onFailure { ex ->
                    loadError = ex.message
                    lorebooks = emptyList()
                }
        }
    }

    LaunchedEffect(Unit) { reload() }

    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarHost(snackbarHostState)

    val importLauncher = rememberFilePickerLauncher(
        mode = PickerMode.Multiple(),
        type = PickerType.File(extensions = listOf("json")),
    ) { files ->
        if (files.isNullOrEmpty()) return@rememberFilePickerLauncher
        scope.launch {
            files.forEach { file ->
                val result = runCatching { file.importLorebook(provider) }
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
        title = "Imported lorebooks",
        description = "Import, list and delete lorebooks.",
        trailingContent = {
            FilledTonalIconButton(onClick = { importLauncher.launch() }) {
                Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Import lorebook")
            }
        },
        bottomContent = {
            when {
                loadError != null -> Text(text = "Failed to load lorebooks: $loadError")
                lorebooks == null -> FullSizeProgressIndicator()
                lorebooks!!.isEmpty() -> Text(text = "No lorebooks available.")
                else -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    lorebooks!!.forEach { lorebook ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val title = lorebook.name ?: lorebook.id ?: "Unnamed lorebook"
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
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete lorebook")
                            }
                        }
                    }
                }
            }
        }
    )

    // Confirm deletion dialog
    pendingDelete?.let { toDelete ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete lorebook?") },
            text = { Text("Are you sure you want to delete \"${toDelete.name ?: toDelete.id}\"? This cannot be undone.") },
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
expect suspend fun PlatformFile.importLorebook(provider: FileSystemLorebookProvider)
