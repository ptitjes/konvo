package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.roleplay.*

/**
 * A selector for character greetings.
 *
 * @param selectedGreetingIndex The currently selected greeting index
 * @param onGreetingIndexSelected Callback for when a greeting is selected
 * @param character The character whose greetings are being selected
 * @param modifier The modifier to apply to this component
 */
@Composable
fun CharacterGreetingSelector(
    selectedGreetingIndex: Int?,
    onGreetingIndexSelected: (Int?) -> Unit,
    character: CharacterCard,
    modifier: Modifier = Modifier,
    userName: String,
) {
    GenericSelector(
        label = "Greeting",
        selectedItem = selectedGreetingIndex,
        onSelectItem = onGreetingIndexSelected,
        options = listOf<Int?>(null) + character.greetings.indices.toList(),
        itemLabeler = { indexOrNull ->
            if (indexOrNull == null) {
                "Random Greeting"
            } else {
                val greeting = character.greetings[indexOrNull].replaceTags(userName, character.name)
                val preview = if (greeting.length > 100) greeting.take(100) + "..." else greeting
                "Greeting ${indexOrNull + 1}: $preview"
            }
        },
        modifier = modifier,
    )
}
