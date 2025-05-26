package io.github.ptitjes.konvo.frontend.discord

import dev.kord.common.annotation.*
import dev.kord.common.entity.*
import dev.kord.core.*
import dev.kord.core.behavior.*
import dev.kord.core.behavior.channel.*
import dev.kord.core.entity.channel.*
import dev.kord.core.entity.interaction.*
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.*
import dev.kord.core.event.message.*
import dev.kord.gateway.*
import dev.kord.rest.builder.component.*
import dev.kord.rest.builder.interaction.*
import dev.kord.rest.builder.message.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.frontend.discord.components.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*
import io.github.ptitjes.konvo.frontend.discord.utils.*
import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.uuid.*

suspend fun Konvo.discordBot(token: String) {
    KonvoBot(this, token).start()
}

class KonvoBot(
    private val konvo: Konvo,
    private val token: String,
) : CoroutineScope {
    val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = konvo.coroutineContext + job

    suspend fun start() {
        val kord = Kord(token)

        kord.registerCommandsAndHandlers()

        kord.login {
            @OptIn(PrivilegedIntent::class)
            intents += Intent.MessageContent
        }
    }

    private suspend fun Kord.registerCommandsAndHandlers() {
        on<MessageCreateEvent> {
            val message = message
            val author = message.author
            if (author == null || author.id == kord.selfId) return@on

            handleConversationMessage(this)
        }

        @Suppress("UnusedFlow") createGlobalApplicationCommands {
            input("konvo", "Create a new conversation") {
                defaultMemberPermissions = Permissions {
                    +Permission.ManageMessages
                }

                subCommand("start", "Start a public conversation")

                subCommand("channel", "Start a private conversation channel") {
                    string("channel", "The new channel name") {
                        required = false
                    }
                }
            }
        }

        on<GuildChatInputCommandInteractionCreateEvent> {
            val command = interaction.command

            when (command.rootName) {
                "konvo" -> {
                    command as SubCommand
                    when (command.name) {
                        "start" -> interaction.startConversation()
                        "channel" -> interaction.newConversationChannel(
                            channel = command.strings["channel"],
                        )
                    }
                }
            }
        }
    }

    private suspend fun GuildChatInputCommandInteraction.startConversation() {
        @OptIn(KordUnsafe::class)
        val response = deferEphemeralResponseUnsafe()

        response.conversationBuilderWizard(konvo, user) { configuration ->
            val conversation = initiateConversation(channel, configuration)

            return@conversationBuilderWizard {
                conversationStartMessage(conversation = conversation)
            }
        }
    }

    private suspend fun GuildChatInputCommandInteraction.newConversationChannel(channel: String?) {
        @OptIn(KordUnsafe::class)
        val response = deferEphemeralResponseUnsafe()

        response.conversationBuilderWizard(konvo, user) { configuration ->
            val channelName = channel ?: newChannelName(configuration.mode)

            val newChannel = guild.createTextChannel(channelName) {
                nsfw = true
            }

            val conversation = initiateConversation(newChannel, configuration)

            return@conversationBuilderWizard {
                conversationStartMessage(conversation = conversation, newChannel = newChannel)
            }
        }
    }

    val perChannelConversation = mutableMapOf<Snowflake, Conversation>()

    private suspend fun initiateConversation(
        channel: MessageChannelBehavior,
        configuration: ConversationConfiguration,
    ): Conversation {
        val conversation = konvo.createConversation(configuration)

        perChannelConversation.put(channel.id, conversation)

        channel.createMessage {
            messageFlags { +MessageFlag.IsComponentsV2 }
            conversationStartMessage(conversation = conversation, fullSizeCharacterAvatar = true)
        }

        launch {
            val assistantProcessing = channel.typingToggler()

            for (event in conversation.assistantEvents) {
                when (event) {
                    AssistantEvent.Processing -> assistantProcessing.start()

                    is AssistantEvent.Message -> {
                        assistantProcessing.stop()
                        val content = event.content.maybeSplitDiscordContent()
                        content.forEach { channel.createMessage(it) }
                    }

                    is AssistantEvent.ToolUsePermission -> {
                        assistantProcessing.stop()
                        channel.askForToolUse(event) {
                            assistantProcessing.start()
                        }
                    }

                    is AssistantEvent.ToolUseResult -> {
                        assistantProcessing.stop()
                        channel.notifyToolUse(event)
                        assistantProcessing.start()
                    }
                }
            }
        }

        return conversation
    }

    private suspend fun handleConversationMessage(event: MessageCreateEvent) = coroutineScope {
        val message = event.message
        val channel = message.channel
        val conversation = perChannelConversation[channel.id] ?: return@coroutineScope

        conversation.userEvents.send(message.content)
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun newChannelName(configuration: ConversationModeConfiguration): String = when (configuration) {
    is RoleplayingModeConfiguration -> "cai-${configuration.character.name}-${Uuid.random()}"
    else -> "ai-${Uuid.random()}"
}

private fun MessageBuilder.conversationStartMessage(
    conversation: Conversation,
    newChannel: TextChannel? = null,
    fullSizeCharacterAvatar: Boolean = false,
) {
    val configuration = conversation.configuration

    container {
        accentColor = DiscordConstants.KonvoColor

        textDisplay {
            content = buildString {
                append("### Conversation started in ")
                append(if (newChannel != null) "channel ${newChannel.link}" else "this channel")
                appendLine(".")
            }
        }

        textDisplay {
            content = buildString {
                append("**Mode:** ")
                appendLine(ConversationMode.forConfiguration(configuration).label)
            }
        }

        when (configuration) {
            is QuestionAnswerModeConfiguration -> {
                val modelName = configuration.modelCard.shortName
                val toolNames = configuration.tools.map { it.name }

                textDisplay {
                    content = buildString {
                        appendLine("**Model:** $modelName")
                        appendLine("**Tools:** ${toolNames.joinToString()}")
                    }
                }
            }

            is RoleplayingModeConfiguration -> {
                val modelName = configuration.modelCard.shortName
                val characterName = configuration.character.name
                val characterUrl = configuration.character.avatarUrl
                val userName = configuration.userName

                fun conversationDescriptionString(): String = buildString {
                    appendLine("**Model:** $modelName")
                    appendLine("**Character:** $characterName")
                    appendLine("**Username:** $userName")
                }

                if (fullSizeCharacterAvatar) {
                    textDisplay { content = conversationDescriptionString() }

                    if (characterUrl != null) {
                        mediaGallery {
                            item(characterUrl) {
                                description = characterName
                                spoiler = true
                            }
                        }
                    }
                } else {
                    section {
                        textDisplay { content = conversationDescriptionString() }

                        if (characterUrl != null) {
                            thumbnailAccessory {
                                url = characterUrl
                                spoiler = true
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun MessageChannelBehavior.askForToolUse(
    event: AssistantEvent.ToolUsePermission,
    onAllCallsAcknowledged: suspend () -> Unit,
) {
    val callsToCheck = event.calls.toMutableList()

    createEphemeralMessage {
        suspend fun finished() {
            delete()
            onAllCallsAcknowledged()
        }

        suspend fun HandlerScope<*>.allowToolCall(call: VetoableToolCall, allowed: Boolean) {
            acknowledge()
            if (allowed) call.allow() else call.reject()
            callsToCheck.remove(call)
            if (callsToCheck.isEmpty()) finished() else update()
        }

        container {
            this.accentColor = DiscordConstants.KonvoColor

            callsToCheck.forEach { call ->
                textDisplay {
                    content = buildString {
                        appendLine("-# Called **${call.tool}**")
                        if (call.arguments.isNotEmpty()) {
                            call.arguments.forEach { (name, value) ->
                                appendLine("> -# **$name:** $value")
                            }
                        }
                    }
                }

                actionRow {
                    interactionButton(
                        style = ButtonStyle.Danger,
                        onClick = { allowToolCall(call, true) }
                    ) {
                        label = "Allow"
                    }
                    interactionButton(
                        style = ButtonStyle.Success,
                        onClick = { allowToolCall(call, false) }
                    ) {
                        label = "Reject"
                    }
                }
            }
        }
    }
}

private suspend fun MessageChannelBehavior.notifyToolUse(event: AssistantEvent.ToolUseResult) {
    if (event.result is ToolCallResult.Success) {
        createMessage {
            messageFlags { +MessageFlag.IsComponentsV2 }

            container {
                accentColor = DiscordConstants.KonvoColor

                textDisplay {
                    content = buildString {
                        appendLine("-# Called **${event.call.name}**")
                        if (event.call.arguments.isNotEmpty()) {
                            event.call.arguments.forEach { (name, value) ->
                                appendLine("> -# **$name:** $value")
                            }
                        }
                    }
                }
            }
        }
    }
}
