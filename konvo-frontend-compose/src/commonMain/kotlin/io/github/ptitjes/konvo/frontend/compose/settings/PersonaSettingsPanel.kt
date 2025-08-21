package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import io.github.ptitjes.konvo.frontend.compose.components.settings.*
import org.kodein.di.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaSettingsPanel(
    settings: PersonaSettings,
    updateSettings: (updater: (previous: PersonaSettings) -> PersonaSettings) -> Unit,
) {
    val lorebookManager by rememberInstance<LorebookManager>()
    val lorebooks by lorebookManager.lorebooks.collectAsState(initial = emptyList())

    fun addPersona(persona: Persona) {
        updateSettings { prev -> prev.copy(personas = prev.personas + persona) }
    }

    fun updatePersona(oldName: String, transform: (Persona) -> Persona) {
        updateSettings { prev ->
            val list = prev.personas.toMutableList()
            val index = list.indexOfFirst { it.name == oldName }
            if (index >= 0) {
                list[index] = transform(list[index])
                prev.copy(personas = list)
            } else prev
        }
    }

    fun removePersona(name: String) {
        updateSettings { prev -> prev.copy(personas = prev.personas.filterNot { it.name == name }) }
    }

    var openSheet by remember { mutableStateOf<PersonaSheetState>(PersonaSheetState.Closed) }
    var pendingDeletion by remember { mutableStateOf<Persona?>(null) }

    SettingsBox(
        title = "Personas",
        description = "Add, remove, and edit personas.",
        trailingContent = {
            FilledTonalIconButton(onClick = { openSheet = PersonaSheetState.Adding }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add persona")
            }
        },
        bottomContent = {
            if (settings.personas.isEmpty()) {
                Text(
                    text = "No personas configured.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    settings.personas.sortedBy { it.name.lowercase() }.forEach { persona ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = persona.name, style = MaterialTheme.typography.titleMedium)
                                val subtitle = buildList {
                                    add("Nickname: ${persona.nickname}")
                                    if (persona.defaultLorebookId != null) {
                                        add("With lorebook")
                                    }
                                }.joinToString(" • ")
                                Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
                            }

                            IconButton(onClick = { openSheet = PersonaSheetState.Editing(persona.name) }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit persona")
                            }

                            IconButton(onClick = { pendingDeletion = persona }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete persona")
                            }
                        }
                    }
                }
            }
        },
    )

    pendingDeletion?.let { p ->
        AlertDialog(
            onDismissRequest = { pendingDeletion = null },
            title = { Text("Delete persona?") },
            text = { Text("Are you sure you want to delete \"${p.name}\"?\nThis action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    removePersona(p.name)
                    if (openSheet is PersonaSheetState.Editing && (openSheet as PersonaSheetState.Editing).name == p.name) {
                        openSheet = PersonaSheetState.Closed
                    }
                    pendingDeletion = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDeletion = null }) { Text("Cancel") } },
        )
    }

    if (openSheet !is PersonaSheetState.Closed) {
        ModalBottomSheet(
            onDismissRequest = { openSheet = PersonaSheetState.Closed },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            when (val sheet = openSheet) {
                is PersonaSheetState.Adding -> {
                    PersonaEditor(
                        existingNames = settings.personas.map { it.name }.toSet(),
                        lorebooks = lorebooks,
                        onSubmit = { name, nickname, lorebook ->
                            addPersona(
                                Persona(
                                    name = name,
                                    nickname = nickname,
                                    defaultLorebookId = lorebook?.id,
                                )
                            )
                            openSheet = PersonaSheetState.Closed
                        },
                    )
                }

                is PersonaSheetState.Editing -> {
                    val current = settings.personas.firstOrNull { it.name == sheet.name }
                    if (current != null) {
                        PersonaEditor(
                            initialName = current.name,
                            initialNickname = current.nickname,
                            initialLorebook = lorebooks.firstOrNull { it.id == current.defaultLorebookId },
                            existingNames = (settings.personas.map { it.name }.toSet() - current.name),
                            lorebooks = lorebooks,
                            editing = true,
                            onRemove = { pendingDeletion = current },
                            onSubmit = { newName, newNickname, lorebook ->
                                updatePersona(sheet.name) { _ ->
                                    current.copy(
                                        name = newName,
                                        nickname = newNickname,
                                        defaultLorebookId = lorebook?.id,
                                    )
                                }
                                openSheet = PersonaSheetState.Editing(newName)
                            },
                        )
                    } else {
                        openSheet = PersonaSheetState.Closed
                    }
                }

                is PersonaSheetState.Closed -> {}
            }
        }
    }
}

@Composable
private fun PersonaEditor(
    existingNames: Set<String>,
    lorebooks: List<Lorebook>,
    onSubmit: (name: String, nickname: String, lorebook: Lorebook?) -> Unit,
    initialName: String = "",
    initialNickname: String = "",
    initialLorebook: Lorebook? = null,
    editing: Boolean = false,
    onRemove: (() -> Unit)? = null,
) {
    var name by remember { mutableStateOf(initialName) }
    var nickname by remember { mutableStateOf(initialNickname) }
    var selectedLorebook by remember { mutableStateOf<Lorebook?>(initialLorebook) }

    fun valid(): Boolean = name.isNotBlank() && nickname.isNotBlank() && name !in existingNames

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                isError = name.isBlank() || existingNames.contains(name),
                singleLine = true,
            )

            if (editing && onRemove != null) {
                IconButton(
                    modifier = Modifier.offset(y = 4.dp),
                    onClick = onRemove,
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove persona")
                }
            }
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Nickname") },
            isError = nickname.isBlank(),
            singleLine = true,
            visualTransformation = VisualTransformation.None,
        )

        LorebookSelector(
            label = "Default Lorebook",
            selectedLorebook = selectedLorebook,
            onLorebookSelected = { selectedLorebook = it },
            lorebooks = lorebooks,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth(),
        ) {
            FilledTonalButton(onClick = { onSubmit(name.trim(), nickname.trim(), selectedLorebook) }, enabled = valid()) {
                Text(if (editing) "Save" else "Add")
            }
        }
    }
}

private sealed interface PersonaSheetState {
    data object Closed : PersonaSheetState
    data object Adding : PersonaSheetState
    data class Editing(val name: String) : PersonaSheetState
}
