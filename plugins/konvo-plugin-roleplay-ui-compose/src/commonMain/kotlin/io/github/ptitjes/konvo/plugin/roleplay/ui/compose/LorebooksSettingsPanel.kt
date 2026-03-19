package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*
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

private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanelScope.LorebooksSettingsPanel() {
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

    val importLauncher = rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        type = FileKitType.File(extensions = listOf("json")),
    ) { files ->
        if (files.isNullOrEmpty()) return@rememberFilePickerLauncher
        scope.launch {
            files.forEach { file ->
                runCatching { provider.add(file.toKotlinxIoPath()) }
                    .onSuccess { reload() }
                    .onFailure { exception ->
                        logger.error(exception) { "Failed to import lorebook" }
                        showSnackbar("Failed to import lorebook:\n${exception.message ?: "Unknown error"}")
                    }
            }
        }
    }

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
            when {
                loadError != null -> Text(text = i18n.roleplay.failedToLoadLorebooks(loadError!!))
                lorebooks == null -> FullSizeProgressIndicator()
                lorebooks!!.isEmpty() -> Text(text = i18n.roleplay.noLorebooksAvailable)
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
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(i18n.roleplay.deleteLorebookDialogTitle) },
            text = { Text(i18n.roleplay.deleteLorebookDialogText(toDelete.name ?: toDelete.id ?: "")) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        pendingDelete = null
                        runCatching { provider.delete(toDelete) }
                            .onSuccess { reload() }
                            .onFailure { exception ->
                                logger.error(exception) { "Failed to delete lorebook" }
                                showSnackbar("Failed to delete lorebook:\n${exception.message ?: "Unknown error"}")
                            }
                    }
                }) { Text(i18n.roleplay.deleteConfirm) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(i18n.roleplay.cancel) } },
        )
    }
}
