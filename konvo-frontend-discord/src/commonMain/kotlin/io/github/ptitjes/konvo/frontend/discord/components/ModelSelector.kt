package io.github.ptitjes.konvo.frontend.discord.components

import ai.koog.prompt.markdown.*
import dev.kord.rest.builder.component.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*
import io.github.ptitjes.konvo.frontend.discord.utils.*

fun EphemeralComponentContainerBuilder.modelSelector(
    models: List<Model>,
    selectedModel: Model?,
    onSelectModel: suspend (Model) -> Unit,
) {
    textDisplay { content = markdown { bold("Model:") } }

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
                    description = buildString {
                        append("Context: "); append(model.contextLengthString ?: "?"); append("; ")
                        append("Parameters: "); append(model.parameterCountString ?: "?"); append("; ")
                        append("Size: "); append(model.sizeString); append("; ")
                        append("Quantization: "); append(model.quantizationLevel ?: "?"); append("; ")
                        append("Tools: "); append(if (model.supportsTools) "yes" else "no")
                    }.maybeEllipsisDiscordLabel()
                    default = model == selectedModel
                }
            }
        }
    }

    if (selectedModel != null) {
        textDisplay {
            content = markdown {
                blockquote {
                    subscript {
                        line { bold("Name:"); space(); text(selectedModel.shortName) }
                        line { bold("Context length:"); space(); text(selectedModel.contextLengthString ?: "?") }
                        line { bold("Parameters:"); space(); text(selectedModel.parameterCountString ?: "?") }
                        line { bold("Size:"); space(); text(selectedModel.sizeString) }
                        line { bold("Quantization:"); space(); text(selectedModel.quantizationLevel ?: "?") }
                        line { bold("Tool support:"); space(); text(if (selectedModel.supportsTools) "yes" else "no") }
                    }
                }
            }
        }
    }
}
