package io.github.ptitjes.konvo.frontend.compose.roleplay

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
import io.github.ptitjes.konvo.frontend.compose.toolkit.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
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
        title = strings.roleplay.importedLorebooksTitle,
        description = strings.roleplay.importedLorebooksDescription,
        trailingContent = {
            FilledTonalIconButton(onClick = { importLauncher.launch() }) {
                Icon(imageVector = Icons.Default.FileDownload, contentDescription = strings.roleplay.importLorebookAria)
            }
        },
        bottomContent = {
            when {
                loadError != null -> Text(text = strings.roleplay.failedToLoadLorebooks(loadError!!))
                lorebooks == null -> FullSizeProgressIndicator()
                lorebooks!!.isEmpty() -> Text(text = strings.roleplay.noLorebooksAvailable)
                else -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    lorebooks!!.forEach { lorebook ->
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
                                    val title = lorebook.name ?: lorebook.id ?: strings.roleplay.lorebookUnnamed
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
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = strings.roleplay.deleteLorebookAria)
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
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(strings.roleplay.deleteLorebookDialogTitle) },
            text = { Text(strings.roleplay.deleteLorebookDialogText(toDelete.name ?: toDelete.id ?: "")) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        runCatching { provider.delete(toDelete) }
                        pendingDelete = null
                        reload()
                    }
                }) { Text(strings.roleplay.deleteConfirm) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(strings.roleplay.cancel) } },
        )
    }
}

// Expect/actual helper to import a PlatformFile using the provider
expect suspend fun PlatformFile.importLorebook(provider: FileSystemLorebookProvider)
