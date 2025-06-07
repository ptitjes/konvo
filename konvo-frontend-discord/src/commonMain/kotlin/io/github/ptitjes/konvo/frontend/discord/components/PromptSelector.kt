package io.github.ptitjes.konvo.frontend.discord.components

import dev.kord.rest.builder.component.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*

fun EphemeralComponentContainerBuilder.promptSelector(
    prompts: List<PromptCard>,
    selectedPrompt: PromptCard?,
    onSelectPrompt: suspend (PromptCard) -> Unit,
) {
    textDisplay { content = "**Character:**" }

    actionRow {
        stringSelect(
            onSelect = { selected ->
                acknowledge()
                onSelectPrompt(prompts.first { it.name == selected.first() })
            },
        ) {
            placeholder = "Select a prompt"

            prompts.forEach { prompt ->
                option(label = prompt.name, value = prompt.name) {
                    description = prompt.description?.maybeEllipsisDiscordLabel()
                    default = prompt == selectedPrompt
                }
            }
        }
    }

    if (selectedPrompt != null) {
        fun promptDescriptionString(): String = buildString {
            appendLine("> -# **Name:** ${selectedPrompt.name}")
            appendLine("> -# **Description:** ${(selectedPrompt.description)}")
        }

        textDisplay { content = promptDescriptionString() }
    }
}
