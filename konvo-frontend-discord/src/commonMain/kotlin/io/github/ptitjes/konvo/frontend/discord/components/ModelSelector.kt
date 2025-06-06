package io.github.ptitjes.konvo.frontend.discord.components

import dev.kord.rest.builder.component.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*
import io.github.ptitjes.konvo.frontend.discord.utils.*

fun EphemeralComponentContainerBuilder.modelSelector(
    models: List<ModelCard>,
    selectedModel: ModelCard?,
    onSelectModel: suspend (ModelCard) -> Unit,
) {
    textDisplay { content = "**Model:**" }

    actionRow {
        stringSelect(
            onSelect = { selected ->
                acknowledge()
                onSelectModel(models.first { it.name == selected.first() })
            },
        ) {
            placeholder = "Select a model"

            models.forEach { model ->
                option(label = model.shortName, value = model.name) {
                    description = model.description
                    default = model == selectedModel
                }
            }
        }
    }

    if (selectedModel != null) {
        textDisplay {
            content = buildString {
                appendLine("> -# **Name:** ${selectedModel.shortName}")
                appendLine("> -# **Context length:** ${(selectedModel.contextLengthString)}")
                appendLine("> -# **Parameters:** ${(selectedModel.parameterCount)}")
                appendLine("> -# **Size:** ${(selectedModel.sizeString)}")
                appendLine("> -# **Quantization:** ${(selectedModel.quantizationLevel)}")
            }
        }
    }
}
