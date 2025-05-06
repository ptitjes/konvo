package io.github.ptitjes.konvo.frontend.discord.components

import dev.kord.rest.builder.component.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.conversation.replaceTags
import io.github.ptitjes.konvo.frontend.discord.toolkit.*

fun EphemeralComponentContainerBuilder.characterGreetingsSelector(
    character: Character,
    selectedGreetingIndex: Int?,
    onSelectGreeting: suspend (Int?) -> Unit,
) {
    textDisplay { content = "**Character greeting:**" }

    val greetings = character.greetings
    if (greetings.size > 1) {
        actionRow {
            stringSelect(
                onSelect = { selected ->
                    acknowledge()
                    onSelectGreeting(
                        selected.first().takeIf { it != "random-greeting" }
                            ?.removePrefix("greeting-")?.toInt()
                    )
                },
            ) {
                placeholder = "Select a character"

                option(label = "Select randomly", value = "random-greeting") {
                    default = selectedGreetingIndex == null
                }

                character.greetings.forEachIndexed { index, greeting ->
                    option(label = "Greeting #$index", value = "greeting-$index") {
                        val escaped = greeting.replaceTags("USER", character.name)
                        description = escaped.maybeEllipsisDiscordLabel()
                        default = index == selectedGreetingIndex
                    }
                }
            }

        }
    }

    if (greetings.size == 1 || selectedGreetingIndex != null) {
        textDisplay {
            val greeting = character.greetings[selectedGreetingIndex ?: 0]
            val escaped = greeting.replaceTags("USER", character.name)
            content = buildString {
                escaped.maybeEllipsisDiscordContent().lines().forEach { line ->
                    appendLine("> ${line.takeUnless { it.isBlank() }?.let { "-# $it" } ?: ""}")
                }
            }
        }
    }
}
