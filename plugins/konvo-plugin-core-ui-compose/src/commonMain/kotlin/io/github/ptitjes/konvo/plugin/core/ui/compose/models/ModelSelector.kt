package io.github.ptitjes.konvo.plugin.core.ui.compose.models

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.plugin.core.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import org.jetbrains.compose.resources.*

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
    label: String? = strings.models.modelLabel,
    selectedModel: ModelCard,
    onModelSelected: (ModelCard) -> Unit,
    models: List<ModelCard>,
    modifier: Modifier = Modifier,
) {
    GenericSelector(
        modifier = modifier,
        label = label,
        selectedItem = selectedModel,
        onSelectItem = onModelSelected,
        options = models,
        itemLabeler = { it.name },
        itemOption = { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                item.provider.name?.let { providerName ->
                    val providerIcon = ModelProviderIcons.iconFor(item.provider)

                    FilterChip(
                        label = {
                            Text(
                                text = providerName,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        leadingIcon = providerIcon?.let {
                            {
                                Icon(
                                    painter = painterResource(it),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        },
                        onClick = {},
                        selected = false,
                        enabled = false,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                Text(
                    text = item.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
    )
}
