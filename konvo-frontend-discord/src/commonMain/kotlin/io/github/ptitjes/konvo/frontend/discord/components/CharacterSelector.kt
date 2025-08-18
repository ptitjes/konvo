package io.github.ptitjes.konvo.frontend.discord.components

import ai.koog.prompt.markdown.*
import dev.kord.rest.builder.component.*
import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*

fun EphemeralComponentContainerBuilder.characterSelector(
    characters: List<CharacterCard>,
    selectedCharacter: CharacterCard?,
    onSelectCharacter: suspend (CharacterCard) -> Unit,
) {
    textDisplay { content = markdown { bold("Character:") } }

    actionRow {
        stringSelect(
            onSelect = { selected ->
                acknowledge()
                onSelectCharacter(characters.first { it.id == selected.first() })
            },
        ) {
            placeholder = "Select a character"

            characters.forEach { character ->
                option(label = character.name, value = character.id) {
                    val tags = character.tags.joinToString(", ")
                    description = tags.maybeEllipsisDiscordLabel()
                    default = character == selectedCharacter
                }
            }
        }
    }

    if (selectedCharacter != null) {
        fun characterDescriptionString(): String = markdown {
            blockquote {
                subscript {
                    line { bold("Name:"); space(); text(selectedCharacter.name) }
                    line { bold("Tags:"); space(); text(selectedCharacter.tags.joinToString(", ")) }
                }
            }
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
