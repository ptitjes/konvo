package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.roleplay.*

/**
 * A selector for personas.
 *
 * - Displays persona name with nickname as a subtitle chip.
 * - Selection is identified by persona name; conversations will still use the persona nickname as user name.
 *
 * @param label Optional label for the field (defaults to "Persona")
 * @param selectedPersona The currently selected persona
 * @param onPersonaSelected Callback invoked when a persona is selected
 * @param personas The list of available personas
 * @param modifier Modifier to apply to the component
 */
@Composable
fun PersonaSelector(
    label: String? = "Persona",
    selectedPersona: Persona,
    onPersonaSelected: (Persona) -> Unit,
    personas: List<Persona>,
    modifier: Modifier = Modifier,
) {
    GenericSelector(
        modifier = modifier,
        label = label,
        selectedItem = selectedPersona,
        onSelectItem = onPersonaSelected,
        options = personas,
        itemLabeler = { it.name },
        itemOption = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {},
                    label = { Text(text = it.nickname, style = MaterialTheme.typography.bodySmall) },
                    enabled = false,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(
                    text = it.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
    )
}
