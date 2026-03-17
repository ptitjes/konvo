package io.github.ptitjes.konvo.plugin.core.ui.compose.roleplay

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.plugin.core.roleplay.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.utils.*
import org.jetbrains.compose.resources.*
import org.kodein.di.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaSettingsPanel() {
    var settings by rememberMutableSettings(PersonaSettingsKey)

    val lorebookManager by rememberInstance<LorebookManager>()
    val lorebooks by lorebookManager.lorebooks.collectAsState(initial = emptyList())

    fun addPersona(persona: Persona) {
        settings = settings.copy(personas = settings.personas + persona)
    }

    fun updatePersona(oldName: String, transform: (Persona) -> Persona) {
        settings = settings.copy(
            personas = settings.personas.mutate {
                val index = indexOfFirst { it.name == oldName }
                if (index >= 0) {
                    set(index, transform(get(index)))
                }
            }
        )
    }

    fun removePersona(name: String) {
        settings = settings.copy(personas = settings.personas.filterNot { it.name == name })
    }

    var openSheet by remember { mutableStateOf<PersonaSheetState>(PersonaSheetState.Closed) }
    var pendingDeletion by remember { mutableStateOf<Persona?>(null) }

    SettingsBox(
        title = i18n.roleplay.personasTitle,
        description = i18n.roleplay.personasDescription,
        trailingContent = {
            FilledTonalIconButton(onClick = { openSheet = PersonaSheetState.Adding }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = i18n.roleplay.addPersonaAria,
                )
            }
        },
        bottomContent = {
            if (settings.personas.isEmpty()) {
                Text(
                    text = i18n.roleplay.noPersonasConfigured,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    settings.personas.sortedBy { it.name.lowercase() }.forEach { persona ->
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
                                    Text(text = persona.name, style = MaterialTheme.typography.titleMedium)
                                    val subtitle = buildList {
                                        add(i18n.roleplay.nicknamePrefix(persona.nickname))
                                        if (persona.defaultLorebookId != null) {
                                            add(i18n.roleplay.withLorebook)
                                        }
                                    }.joinToString(" • ")
                                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
                                }

                                IconButton(onClick = { openSheet = PersonaSheetState.Editing(persona.name) }) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_edit),
                                        contentDescription = i18n.roleplay.editPersonaAria,
                                    )
                                }

                                IconButton(onClick = { pendingDeletion = persona }) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_delete),
                                        contentDescription = i18n.roleplay.deletePersonaAria,
                                    )
                                }
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
            title = { Text(i18n.roleplay.deletePersonaDialogTitle) },
            text = { Text(i18n.roleplay.deletePersonaDialogText(p.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        removePersona(p.name)
                        val sheet = openSheet
                        if (sheet is PersonaSheetState.Editing && sheet.name == p.name) {
                            openSheet = PersonaSheetState.Closed
                        }
                        pendingDeletion = null
                    },
                ) { Text(i18n.roleplay.deleteConfirm) }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDeletion = null },
                ) {
                    Text(i18n.roleplay.cancel)
                }
            },
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
    var selectedLorebook by remember { mutableStateOf(initialLorebook) }

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
                modifier = Modifier.height(64.dp).weight(1f),
                value = name,
                onValueChange = { name = it },
                label = { Text(i18n.roleplay.nameLabel) },
                isError = name.isBlank() || existingNames.contains(name),
                singleLine = true,
            )

            if (editing && onRemove != null) {
                IconButton(
                    modifier = Modifier.offset(y = 4.dp),
                    onClick = onRemove,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_delete),
                        contentDescription = i18n.roleplay.removePersonaAria,
                    )
                }
            }
        }

        OutlinedTextField(
            modifier = Modifier.height(64.dp).fillMaxWidth(),
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text(i18n.roleplay.nicknameLabel) },
            isError = nickname.isBlank(),
            singleLine = true,
            visualTransformation = VisualTransformation.None,
        )

        LorebookSelector(
            label = i18n.roleplay.defaultLorebookLabel,
            selectedLorebook = selectedLorebook,
            onLorebookSelected = { selectedLorebook = it },
            lorebooks = lorebooks,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth(),
        ) {
            FilledTonalButton(
                onClick = { onSubmit(name.trim(), nickname.trim(), selectedLorebook) },
                enabled = valid()
            ) {
                Text(if (editing) i18n.roleplay.saveAction else i18n.roleplay.addAction)
            }
        }
    }
}

private sealed interface PersonaSheetState {
    data object Closed : PersonaSheetState
    data object Adding : PersonaSheetState
    data class Editing(val name: String) : PersonaSheetState
}
