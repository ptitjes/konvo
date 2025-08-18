package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.core.prompts.*

/**
 * A selector for prompts.
 *
 * @param selectedPrompt The currently selected prompt
 * @param onPromptSelected Callback for when a prompt is selected
 * @param prompts List of available prompts
 * @param modifier The modifier to apply to this component
 */
@Composable
fun PromptSelector(
    selectedPrompt: PromptCard,
    onPromptSelected: (PromptCard) -> Unit,
    prompts: List<PromptCard>,
    modifier: Modifier = Modifier,
) {
    GenericSelector(
        label = "Prompt",
        selectedItem = selectedPrompt,
        onSelectItem = onPromptSelected,
        options = prompts,
        itemLabeler = { it.name },
        modifier = modifier,
    )
}
