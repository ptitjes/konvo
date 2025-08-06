package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.core.ai.spi.PromptCard

/**
 * A selector for prompts.
 *
 * @param selectedPrompt The currently selected prompt
 * @param onPromptSelected Callback for when a prompt is selected
 * @param prompts List of available prompts
 * @param modifier The modifier to apply to this component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptSelector(
    selectedPrompt: PromptCard?,
    onPromptSelected: (PromptCard) -> Unit,
    prompts: List<PromptCard>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Prompt",
            style = MaterialTheme.typography.titleSmall
        )

        var promptExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = promptExpanded,
            onExpandedChange = { promptExpanded = it }
        ) {
            TextField(
                value = selectedPrompt?.name ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = promptExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
            )

            ExposedDropdownMenu(
                expanded = promptExpanded,
                onDismissRequest = { promptExpanded = false }
            ) {
                prompts.forEach { prompt ->
                    DropdownMenuItem(
                        text = { Text(prompt.name) },
                        onClick = {
                            onPromptSelected(prompt)
                            promptExpanded = false
                        }
                    )
                }
            }
        }
    }
}
