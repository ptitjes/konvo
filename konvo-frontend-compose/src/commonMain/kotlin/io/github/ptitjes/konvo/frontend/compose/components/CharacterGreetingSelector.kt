package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.core.Character

/**
 * A selector for character greetings.
 *
 * @param selectedGreetingIndex The currently selected greeting index
 * @param onGreetingIndexSelected Callback for when a greeting is selected
 * @param character The character whose greetings are being selected
 * @param modifier The modifier to apply to this component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterGreetingSelector(
    selectedGreetingIndex: Int?,
    onGreetingIndexSelected: (Int?) -> Unit,
    character: Character,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Greeting",
            style = MaterialTheme.typography.titleSmall
        )

        var greetingExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = greetingExpanded,
            onExpandedChange = { greetingExpanded = it }
        ) {
            TextField(
                value = selectedGreetingIndex?.let {
                    "Greeting ${it + 1}"
                } ?: "Random Greeting",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = greetingExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
            )

            ExposedDropdownMenu(
                expanded = greetingExpanded,
                onDismissRequest = { greetingExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Random Greeting") },
                    onClick = {
                        onGreetingIndexSelected(null)
                        greetingExpanded = false
                    }
                )

                character.greetings.forEachIndexed { index, greeting ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Greeting ${index + 1}: ${
                                    if (greeting.length > 30) greeting.take(30) + "..." else greeting
                                }"
                            )
                        },
                        onClick = {
                            onGreetingIndexSelected(index)
                            greetingExpanded = false
                        }
                    )
                }
            }
        }
    }
}
