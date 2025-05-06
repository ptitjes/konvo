package io.github.ptitjes.konvo.frontend.discord.components

import dev.kord.rest.builder.component.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*

fun EphemeralComponentContainerBuilder.characterSelector(
    characters: List<Character>,
    selectedCharacter: Character?,
    onSelectCharacter: suspend (Character) -> Unit,
) {
    textDisplay { content = "**Character:**" }

    actionRow {
        stringSelect(
            onSelect = { selected ->
                acknowledge()
                onSelectCharacter(characters.first { it.name == selected.first() })
            },
        ) {
            placeholder = "Select a character"

            characters.forEach { character ->
                option(label = character.name, value = character.name) {
                    val tags = character.tags.joinToString(", ")
                    description = tags.maybeEllipsisDiscordLabel()
                    default = character == selectedCharacter
                }
            }
        }
    }

    if (selectedCharacter != null) {
        fun characterDescriptionString(): String = buildString {
            appendLine("> -# **Name:** ${selectedCharacter.name}")
            appendLine("> -# **Tags:** ${(selectedCharacter.tags.joinToString(", "))}")
        }

        if (selectedCharacter.avatarUrl != null) {
            section {
                textDisplay { content = characterDescriptionString() }

                thumbnailAccessory { url = selectedCharacter.avatarUrl }
            }
        } else {
            textDisplay { content = characterDescriptionString() }
        }
    }
}
