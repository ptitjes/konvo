package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.core.Character

/**
 * A selector for characters.
 *
 * @param selectedCharacter The currently selected character
 * @param onCharacterSelected Callback for when a character is selected
 * @param characters List of available characters
 * @param modifier The modifier to apply to this component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSelector(
    selectedCharacter: Character?,
    onCharacterSelected: (Character) -> Unit,
    characters: List<Character>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Character",
            style = MaterialTheme.typography.titleSmall
        )

        var characterExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = characterExpanded,
            onExpandedChange = { characterExpanded = it }
        ) {
            TextField(
                value = selectedCharacter?.name ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = characterExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
            )

            ExposedDropdownMenu(
                expanded = characterExpanded,
                onDismissRequest = { characterExpanded = false }
            ) {
                characters.forEach { character ->
                    DropdownMenuItem(
                        text = { Text(character.name) },
                        onClick = {
                            onCharacterSelected(character)
                            characterExpanded = false
                        }
                    )
                }
            }
        }
    }
}
