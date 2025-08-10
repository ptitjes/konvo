package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.core.ai.spi.CharacterCard

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
) {
    GenericSelector(
        label = "Greeting",
        selectedItem = selectedGreetingIndex,
        onSelectItem = onGreetingIndexSelected,
        options = listOf<Int?>(null) + character.greetings.indices.toList<Int>(),
        itemLabeler = { indexOrNull ->
            if (indexOrNull == null) {
                "Random Greeting"
            } else {
                val greeting = character.greetings[indexOrNull]
                val preview = if (greeting.length > 30) greeting.take(30) + "..." else greeting
                "Greeting ${indexOrNull + 1}: $preview"
            }
        },
        modifier = modifier,
    )
}
