package io.github.ptitjes.konvo.frontend.discord.components

import ai.koog.prompt.markdown.*
import dev.kord.rest.builder.component.*
import io.github.ptitjes.konvo.core.prompts.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*

fun EphemeralComponentContainerBuilder.promptSelector(
    prompts: List<PromptCard>,
    selectedPrompt: PromptCard?,
    onSelectPrompt: suspend (PromptCard) -> Unit,
) {
    textDisplay { content = markdown { bold("Prompt:") } }

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
        fun promptDescriptionString(): String = markdown {
            blockquote {
                subscript {
                    line { bold("Name:"); space(); text(selectedPrompt.name) }
                    selectedPrompt.description?.let { line { bold("Description:"); space(); text(it) } }
                }
            }
        }

        textDisplay { content = promptDescriptionString() }
    }
}
