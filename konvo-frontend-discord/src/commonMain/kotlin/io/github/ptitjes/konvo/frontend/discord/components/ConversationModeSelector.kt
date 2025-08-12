package io.github.ptitjes.konvo.frontend.discord.components

import ai.koog.prompt.markdown.*
import dev.kord.rest.builder.component.*
import io.github.ptitjes.konvo.core.conversation.agents.*
import io.github.ptitjes.konvo.frontend.discord.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*

enum class ConversationMode(
    val label: String,
) {
    QuestionAnswer("Question/Answer"),
    Roleplay("Role-play"),
    ;

    fun createBuilder(): ConversationModeBuilder = when (this) {
        QuestionAnswer -> QuestionAnswerModeBuilder()
        Roleplay -> RoleplayModeBuilder()
    }

    companion object {
        val default = QuestionAnswer

        fun forBuilder(modeBuilder: ConversationModeBuilder): ConversationMode = when (modeBuilder) {
            is QuestionAnswerModeBuilder -> QuestionAnswer
            is RoleplayModeBuilder -> Roleplay
        }

        fun forConfiguration(configuration: AgentConfiguration): ConversationMode = when (configuration) {
            is QuestionAnswerAgentConfiguration -> QuestionAnswer
            is RoleplayAgentConfiguration -> Roleplay
            is NoAgentConfiguration -> error("No agent configuration provided")
        }
    }
}

fun EphemeralComponentContainerBuilder.conversationModeSelector(
    selectedMode: ConversationMode?,
    onSelectMode: suspend (ConversationMode) -> Unit,
) {
    textDisplay { content = markdown { bold("Conversation mode:") } }

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
