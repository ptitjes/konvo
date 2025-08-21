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
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.model.Event
import io.github.ptitjes.konvo.core.conversation.storage.*
import io.github.ptitjes.konvo.core.util.*
import io.github.ptitjes.konvo.frontend.discord.components.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*
import io.github.ptitjes.konvo.frontend.discord.utils.*
import kotlinx.coroutines.*
import org.kodein.di.*
import kotlin.coroutines.*
import kotlin.time.*
import kotlin.uuid.*

suspend fun CoroutineScope.discordBot(konvo: Konvo, token: String) {
    KonvoBot(coroutineContext, konvo, token).start()
}

class KonvoBot(
    parentCoroutineContext: CoroutineContext,
    private val konvo: Konvo,
    private val token: String,
) : CoroutineScope, DIAware {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = parentCoroutineContext + Dispatchers.Default + job

    override val di: DI by lazy { konvo.di }

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
            val conversationView = conversation.newUserView()

            return@conversationBuilderWizard {
                conversationStartMessage(
                    configuration = configuration,
                    conversation = conversationView,
                    konvo = konvo,
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
            val conversationView = conversation.newUserView()

            return@conversationBuilderWizard {
                conversationStartMessage(
                    configuration = configuration,
                    conversation = conversationView,
                    newChannel = newChannel,
                    konvo = konvo,
                )
            }
        }
    }

    val conversationRepository: ConversationRepository by instance()
    val liveConversationsManager: LiveConversationsManager by instance()
    val perChannelConversation = mutableMapOf<Snowflake, LiveConversation>()

    @OptIn(ExperimentalTime::class)
    private suspend fun initiateConversation(
        channel: MessageChannelBehavior,
        configuration: ConversationConfiguration,
    ): LiveConversation {
        val now = SystemTimeProvider.now()

        val conversation = Conversation(
            id = channel.id.toString(),
            title = "Conversation on ${channel.id}",
            createdAt = now,
            updatedAt = now,
            participants = listOf(),
            lastMessagePreview = null,
            messageCount = 0,
            agentConfiguration = configuration.agent,
        )

        conversationRepository.createConversation(conversation)

        val liveConversation = liveConversationsManager.getLiveConversation(conversation.id)
        val conversationView = liveConversation.newUserView()

        perChannelConversation[channel.id] = liveConversation

        channel.createMessage {
            messageFlags { +MessageFlag.IsComponentsV2 }
            conversationStartMessage(
                configuration = configuration,
                conversation = conversationView,
                fullSizeCharacterAvatar = true,
                konvo = konvo,
            )
        }

        launch { channel.handleAssistantEvents(conversationView) }

        return liveConversation
    }

    private suspend fun handleConversationMessage(event: MessageCreateEvent) = coroutineScope {
        val message = event.message
        val channel = message.channel
        val conversation = perChannelConversation[channel.id] ?: return@coroutineScope

        if (message.type != MessageType.Default) return@coroutineScope

        val conversationView = conversation.newUserView()

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
    konvo: Konvo,
) {
    val configuration: AgentConfiguration = configuration.agent

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
                val model = konvo.models.first { it.name == configuration.modelName }

                textDisplay {
                    content = markdown {
                        line {
                            bold("Tools:"); space()
                            text(configuration.mcpServerNames.takeIf { it.isNotEmpty() }?.joinToString() ?: "None")
                        }
                        line { bold("Model:"); space(); text(model.shortName) }
                    }
                }
            }

            is RoleplayAgentConfiguration -> {
                val character = konvo.characters.first { it.id == configuration.characterId }
                val model = konvo.models.first { it.name == configuration.modelName }

                fun conversationDescriptionString(): String = markdown {
                    line { bold("Character:"); space(); text(character.name) }
                    if (character.greetings.isNotEmpty()) line {
                        bold("Character greeting:"); space()
                        text(configuration.characterGreetingIndex?.let { "#$it" } ?: "Random")
                    }
                    line { bold("Username:"); space(); text(configuration.personaName) }
                    line { bold("Model:"); space(); text(model.shortName) }
                }

                val characterName = character.name
                val characterUrl = character.avatarUrl
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

            NoAgentConfiguration -> error("No agent configuration provided.")
        }
    }
}

private suspend fun MessageChannelBehavior.handleAssistantEvents(conversation: ConversationUserView): Nothing =
    coroutineScope {
        val assistantProcessing = typingToggler(this@handleAssistantEvents)

        conversation.events.collect { event ->
            when (event) {
                is Event.AssistantProcessing ->
                    if (event.isProcessing) assistantProcessing.start()
                    else assistantProcessing.stop()

                is Event.AssistantMessage -> {
                    val content = event.content.maybeSplitDiscordContent()
                    content.forEach { createMessage(it) }
                    assistantProcessing.maybeRestart()
                }

                is Event.ToolUseVetting -> {
                    askForToolUse(conversation, event)
                    assistantProcessing.maybeRestart()
                }

                is Event.ToolUseNotification -> {
                    notifyToolUse(event)
                    assistantProcessing.maybeRestart()
                }

                else -> {}
            }
        }
    }

private suspend fun MessageChannelBehavior.askForToolUse(
    conversation: ConversationUserView,
    event: Event.ToolUseVetting,
) {
    val done = CompletableDeferred<Unit>()
    val callsToCheck = event.calls.toMutableList()
    val approvals = mutableMapOf<ToolCall, Boolean>()

    createEphemeralMessage {
        suspend fun finished() {
            // Send approvals and close the ephemeral message
            conversation.sendToolUseApproval(
                vetting = event,
                approvals = approvals.toMap(),
            )
            delete()
            done.complete(Unit)
        }

        suspend fun HandlerScope<*>.recordApproval(call: ToolCall, allowed: Boolean) {
            acknowledge()
            approvals[call] = allowed
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
                        style = ButtonStyle.Success,
                        onClick = { recordApproval(call, true) }
                    ) {
                        label = "Allow"
                    }
                    interactionButton(
                        style = ButtonStyle.Danger,
                        onClick = { recordApproval(call, false) }
                    ) {
                        label = "Reject"
                    }
                }
            }
        }
    }

    done.await()
}

private suspend fun MessageChannelBehavior.notifyToolUse(event: Event.ToolUseNotification) {
    if (event.result is ToolCallResult.Success) {
        createMessage {
            messageFlags { +MessageFlag.IsComponentsV2 }

            container {
                accentColor = DiscordConstants.KonvoColor

                textDisplay {
                    content = markdown {
                        val tool = event.call.tool
                        val arguments = event.call.arguments

                        subscript { text("Agent called tool"); space(); bold(tool) }
                        if (arguments.isNotEmpty()) blockquote {
                            arguments.forEach { (name, value) ->
                                subscript { bold(name); space(); text(value.toString()) }
                            }
                        }
                    }
                }
            }
        }
    }
}
