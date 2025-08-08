package io.github.ptitjes.konvo.frontend.discord

import ai.koog.prompt.markdown.*
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
            val conversationView = conversation.newUiView()

            return@conversationBuilderWizard {
                conversationStartMessage(
                    configuration = configuration,
                    conversation = conversationView,
                )
            }
        }
    }

    private suspend fun GuildChatInputCommandInteraction.newConversationChannel(channel: String?) {
        @OptIn(KordUnsafe::class)
        val response = deferEphemeralResponseUnsafe()

        response.conversationBuilderWizard(konvo, user) { configuration ->
            val channelName = channel ?: newChannelName()

            val newChannel = guild.createTextChannel(channelName) {
                nsfw = true
            }

            val conversation = initiateConversation(newChannel, configuration)
            val conversationView = conversation.newUiView()

            return@conversationBuilderWizard {
                conversationStartMessage(
                    configuration = configuration,
                    conversation = conversationView,
                    newChannel = newChannel,
                )
            }
        }
    }

    val perChannelConversation = mutableMapOf<Snowflake, ActiveConversation>()

    private suspend fun initiateConversation(
        channel: MessageChannelBehavior,
        configuration: ConversationConfiguration,
    ): ActiveConversation {
        val conversation = konvo.createConversation(configuration)
        val conversationView = conversation.newUiView()

        perChannelConversation[channel.id] = conversation

        channel.createMessage {
            messageFlags { +MessageFlag.IsComponentsV2 }
            conversationStartMessage(
                configuration = configuration,
                conversation = conversationView,
                fullSizeCharacterAvatar = true,
            )
        }

        launch { channel.handleAssistantEvents(conversationView) }

        return conversation
    }

    private suspend fun handleConversationMessage(event: MessageCreateEvent) = coroutineScope {
        val message = event.message
        val channel = message.channel
        val conversation = perChannelConversation[channel.id] ?: return@coroutineScope

        if (message.type != MessageType.Default) return@coroutineScope

        val conversationView = conversation.newUiView()

        conversationView.sendMessage(
            content = message.content,
            attachments = message.attachments.map { attachment ->
                val fileName = attachment.filename
                val format = fileName.substringAfterLast('.')
                val isImage = attachment.isImage
                val contentType = attachment.contentType
                val isAudio = contentType?.startsWith("audio/") ?: false

                Attachment(
                    type = when {
                        isImage -> Attachment.Type.Image
                        isAudio -> Attachment.Type.Audio
                        else -> Attachment.Type.Document
                    },
                    url = attachment.url,
                    name = fileName,
                    mimeType = contentType ?: when {
                        isImage -> "image/$format"
                        else -> "application/$format"
                    },
                )
            }
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun newChannelName(): String = "ai-${Uuid.random()}"

private fun MessageBuilder.conversationStartMessage(
    configuration: ConversationConfiguration,
    conversation: ConversationUserView,
    newChannel: TextChannel? = null,
    fullSizeCharacterAvatar: Boolean = false,
) {
    val configuration = configuration.agent

    container {
        accentColor = DiscordConstants.KonvoColor

        textDisplay {
            content = markdown {
                val channelString = if (newChannel != null) "channel ${newChannel.link}" else "this channel"
                h3("Conversation started in $channelString.")
            }
        }

        textDisplay {
            content = markdown {
                line { bold("Mode:"); space(); text(ConversationMode.forConfiguration(configuration).label) }
            }
        }

        when (configuration) {
            is QuestionAnswerAgentConfiguration -> {
                textDisplay {
                    content = markdown {
                        line { bold("Prompt:"); space(); text(configuration.prompt.name) }
                        line {
                            bold("Tools:"); space()
                            text(configuration.tools.takeIf { it.isNotEmpty() }?.joinToString { it.name } ?: "None")
                        }
                        line { bold("Model:"); space(); text(configuration.model.shortName) }
                    }
                }
            }

            is RoleplayingAgentConfiguration -> {
                fun conversationDescriptionString(): String = markdown {
                    line { bold("Character:"); space(); text(configuration.character.name) }
                    if (configuration.character.greetings.isNotEmpty()) line {
                        bold("Character greeting:"); space()
                        text(configuration.characterGreetingIndex?.let { "#$it" } ?: "Random")
                    }
                    line { bold("Username:"); space(); text(configuration.userName) }
                    line { bold("Model:"); space(); text(configuration.model.shortName) }
                }

                val characterName = configuration.character.name
                val characterUrl = configuration.character.avatarUrl
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

private suspend fun MessageChannelBehavior.handleAssistantEvents(conversation: ConversationUserView): Nothing =
    coroutineScope {
        val assistantProcessing = typingToggler(this@handleAssistantEvents)

        conversation.events.collect { event ->
            when (event) {
                is ConversationEvent.AssistantProcessing -> assistantProcessing.start()

                is ConversationEvent.AssistantMessage -> {
                    assistantProcessing.stop()
                    val content = event.content.maybeSplitDiscordContent()
                    content.forEach { createMessage(it) }
                }

                is ConversationEvent.AssistantToolUseVetting -> {
                    assistantProcessing.stop()
                    askForToolUse(event)
                    assistantProcessing.start()
                }

                is ConversationEvent.AssistantToolUseResult -> {
                    assistantProcessing.stop()
                    notifyToolUse(event)
                    assistantProcessing.start()
                }
                else -> {}
            }
        }
    }

private suspend fun MessageChannelBehavior.askForToolUse(event: ConversationEvent.AssistantToolUseVetting) {
    val done = CompletableDeferred<Unit>()
    val callsToCheck = event.calls.toMutableList()

    createEphemeralMessage {
        suspend fun finished() {
            delete()
            done.complete(Unit)
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
                    content = markdown {
                        subscript { text("Agent wants to call"); space(); bold(call.tool) }
                        if (call.arguments.isNotEmpty()) blockquote {
                            call.arguments.forEach { (name, value) ->
                                subscript { bold(name); space(); text(value.toString()) }
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

    done.await()
}

private suspend fun MessageChannelBehavior.notifyToolUse(event: ConversationEvent.AssistantToolUseResult) {
    if (event.result is ToolCallResult.Success) {
        createMessage {
            messageFlags { +MessageFlag.IsComponentsV2 }

            container {
                accentColor = DiscordConstants.KonvoColor

                textDisplay {
                    content = markdown {
                        subscript { text("Agent called tool"); space(); bold(event.tool) }
                        if (event.arguments.isNotEmpty()) blockquote {
                            event.arguments.forEach { (name, value) ->
                                subscript { bold(name); space(); text(value.toString()) }
                            }
                        }
                    }
                }
            }
        }
    }
}
