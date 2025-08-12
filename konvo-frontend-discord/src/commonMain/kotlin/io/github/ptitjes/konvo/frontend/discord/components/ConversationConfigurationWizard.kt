package io.github.ptitjes.konvo.frontend.discord.components

import ai.koog.prompt.markdown.*
import dev.kord.common.entity.*
import dev.kord.core.behavior.*
import dev.kord.core.behavior.interaction.response.*
import dev.kord.rest.builder.message.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.frontend.discord.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*

suspend fun EphemeralMessageInteractionResponseBehavior.conversationBuilderWizard(
    konvo: Konvo,
    member: MemberBehavior,
    createConversation: suspend (configuration: ConversationConfiguration) -> MessageBuilder.() -> Unit,
) = ephemeralWizard(
    initialState = ConversationBuilder()
) { state ->
    if (state.endMessageBuilder == null) {
        val canStartConversation = state.isValid()

        suspend fun updateMode(mode: ConversationModeBuilder) {
            this@ephemeralWizard.updateState(state.copy(mode = mode))
        }

        container {
            accentColor = DiscordConstants.KonvoColor

            textDisplay { content = markdown { h3("Configure the conversation") } }

            separator { }

            separator { divider = false }

            val mode = state.mode

            conversationModeSelector(mode?.let { ConversationMode.forBuilder(it) }) {
                this@ephemeralWizard.updateState(
                    state.copy(
                        mode = it.createBuilder().let { mode ->
                            if (mode is RoleplayModeBuilder) {
                                mode.copy(userName = member.asMember().effectiveName)
                            } else mode
                        },
                    )
                )
            }

            if (mode != null) separator { divider = false }

            when (mode) {
                null -> {}

                is QuestionAnswerModeBuilder -> {
                    promptSelector(konvo.prompts, mode.prompt) {
                        updateMode(mode.copy(prompt = it))
                    }

                    separator { divider = false }

                    toolSelector(konvo.tools, mode.tools) {
                        updateMode(mode.copy(tools = it))
                    }

                    separator { divider = false }

                    val needsToolSupport = mode.tools != null && mode.tools.isNotEmpty()
                    val models = konvo.models.let { models ->
                        if (needsToolSupport) models.filter { it.supportsTools } else models
                    }

                    separator { divider = false }

                    modelSelector(models, mode.model) {
                        updateMode(mode.copy(model = it))
                    }
                }

                is RoleplayModeBuilder -> {
                    characterSelector(konvo.characters, mode.character) {
                        updateMode(mode.copy(character = it, characterGreetingIndex = null))
                    }

                    if (mode.character != null) {
                        characterGreetingsSelector(mode.character, mode.characterGreetingIndex) {
                            updateMode(mode.copy(characterGreetingIndex = it))
                        }
                    }

                    separator { divider = false }

                    section {
                        textDisplay {
                            content = markdown {
                                line { text("You are playing as"); space(); bold(mode.userName ?: "?") }
                            }
                        }
                        interactionButtonAccessory(
                            style = ButtonStyle.Secondary,
                            onClick = {
                                val result = acknowledgeWithModal("User persona") {
                                    actionRow {
                                        textInput(
                                            style = TextInputStyle.Short,
                                            customId = "name",
                                            label = "Name",
                                        ) {
                                            placeholder = "The user name"
                                            value = mode.userName
                                        }
                                    }
                                }

                                if (result == null) error("Modal timed out")
                                val selectedUserName = result["name"] ?: error("User name must be specified")
                                updateMode(mode.copy(userName = selectedUserName))
                            },
                        ) {
                            label = "Edit persona"
                        }
                    }

                    separator { divider = false }

                    modelSelector(konvo.models, mode.model) {
                        updateMode(mode.copy(model = it))
                    }
                }
            }

            separator { divider = false }

            actionRow {
                interactionButton(
                    style = ButtonStyle.Success,
                    onClick = {
                        acknowledge()
                        if (!canStartConversation) return@interactionButton
                        val endMessageBuilder = createConversation(state.build())
                        this@ephemeralWizard.updateState(state.copy(endMessageBuilder = endMessageBuilder))
                    },
                ) {
                    label = "Start conversation"
                    disabled = !canStartConversation
                }
            }
        }
    } else {
        clear()
        state.endMessageBuilder(this)
    }
}
