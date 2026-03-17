package io.github.ptitjes.konvo.plugin.core.ui.compose.roleplay

import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.plugin.core.agents.*
import io.github.ptitjes.konvo.plugin.core.roleplay.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*

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
    personaName: String,
) {
    val greetingLabel = i18n.roleplay.greetingLabel
    val randomGreeting = i18n.roleplay.randomGreeting
    val greetingOptionLabel = i18n.roleplay.greetingOptionLabel

    GenericSelector(
        label = greetingLabel,
        selectedItem = selectedGreetingIndex,
        onSelectItem = onGreetingIndexSelected,
        options = listOf<Int?>(null) + character.greetings.indices.toList(),
        itemLabeler = { indexOrNull ->
            if (indexOrNull == null) {
                randomGreeting
            } else {
                val greeting = character.greetings[indexOrNull].replaceTags(personaName, character.name)
                val preview = if (greeting.length > 100) greeting.take(100) + "..." else greeting
                greetingOptionLabel(indexOrNull, preview)
            }
        },
        modifier = modifier,
    )
}
