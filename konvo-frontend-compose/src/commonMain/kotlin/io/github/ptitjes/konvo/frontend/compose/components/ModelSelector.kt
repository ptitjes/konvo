package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.core.ai.spi.*

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
        modifier = modifier,
    )
}
