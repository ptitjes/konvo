package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.core.*

/**
 * A selector for characters.
 *
 * @param selectedCharacter The currently selected character
 * @param onCharacterSelected Callback for when a character is selected
 * @param characters List of available characters
 * @param modifier The modifier to apply to this component
 */
@Composable
fun CharacterSelector(
    selectedCharacter: Character,
    onCharacterSelected: (Character) -> Unit,
    characters: List<Character>,
    modifier: Modifier = Modifier,
) {
    GenericSelector(
        label = "Character",
        selectedItem = selectedCharacter,
        onSelectItem = onCharacterSelected,
        options = characters,
        itemLabeler = { it.name },
        modifier = modifier,
    )
}
