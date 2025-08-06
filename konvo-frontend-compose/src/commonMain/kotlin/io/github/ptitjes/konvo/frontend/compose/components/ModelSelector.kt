package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.core.ai.spi.ModelCard

/**
 * A selector for models.
 *
 * @param selectedModel The currently selected model
 * @param onModelSelected Callback for when a model is selected
 * @param models List of available models
 * @param modifier The modifier to apply to this component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelector(
    selectedModel: ModelCard?,
    onModelSelected: (ModelCard) -> Unit,
    models: List<ModelCard>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Model",
            style = MaterialTheme.typography.titleSmall
        )

        var modelExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = modelExpanded,
            onExpandedChange = { modelExpanded = it }
        ) {
            TextField(
                value = selectedModel?.name ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
            )

            ExposedDropdownMenu(
                expanded = modelExpanded,
                onDismissRequest = { modelExpanded = false }
            ) {
                models.forEach { model ->
                    DropdownMenuItem(
                        text = { Text(model.name) },
                        onClick = {
                            onModelSelected(model)
                            modelExpanded = false
                        }
                    )
                }
            }
        }
    }
}
