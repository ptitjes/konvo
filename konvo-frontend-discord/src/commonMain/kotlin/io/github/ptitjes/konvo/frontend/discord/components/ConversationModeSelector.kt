package io.github.ptitjes.konvo.frontend.discord.components

import dev.kord.rest.builder.component.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.frontend.discord.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*

enum class ConversationMode(
    val label: String,
) {
    QuestionAnswer("Question/Answer"),
    Roleplaying("Roleplaying"),
    ;

    fun createBuilder(): ConversationModeBuilder = when (this) {
        QuestionAnswer -> QuestionAnswerModeBuilder()
        Roleplaying -> RoleplayingModeBuilder()
    }

    companion object {
        val default = QuestionAnswer

        fun forBuilder(modeBuilder: ConversationModeBuilder): ConversationMode = when (modeBuilder) {
            is QuestionAnswerModeBuilder -> QuestionAnswer
            is RoleplayingModeBuilder -> Roleplaying
        }

        fun forConfiguration(configuration: ConversationModeConfiguration): ConversationMode = when (configuration) {
            is QuestionAnswerModeConfiguration -> QuestionAnswer
            is RoleplayingModeConfiguration -> Roleplaying
        }
    }
}

fun EphemeralComponentContainerBuilder.conversationModeSelector(
    selectedMode: ConversationMode?,
    onSelectMode: suspend (ConversationMode) -> Unit,
) {
    textDisplay { content = "**Conversation mode:**" }

    actionRow {
        stringSelect(
            onSelect = { selected ->
                acknowledge()
                onSelectMode(ConversationMode.valueOf(selected.first()))
            },
        ) {
            placeholder = "Select a conversation mode"

            ConversationMode.entries.forEach { mode ->
                option(label = mode.label, value = mode.name) {
                    default = mode == selectedMode
                }
            }
        }
    }
}
