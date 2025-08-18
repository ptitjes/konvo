package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.models.*

/**
 * A selector for models.
 *
 * @param selectedModel The currently selected model
 * @param onModelSelected Callback for when a model is selected
 * @param models List of available models
 * @param modifier The modifier to apply to this component
 */
@Composable
fun ModelSelector(
    selectedModel: ModelCard,
    onModelSelected: (ModelCard) -> Unit,
    models: List<ModelCard>,
    modifier: Modifier = Modifier,
) {
    GenericSelector(
        label = "Model",
        selectedItem = selectedModel,
        onSelectItem = onModelSelected,
        options = models,
        itemLabeler = { it.name },
        itemOption = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                it.provider.name?.let { providerName ->
                    FilterChip(
                        label = { Text(providerName) },
                        onClick = {},
                        selected = false,
                        enabled = false,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                Text(
                    text = it.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        modifier = modifier,
    )
}
