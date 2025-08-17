@file:OptIn(ExperimentalTime::class)

package io.github.ptitjes.konvo.core.conversation.storage.files

import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.models.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.time.*

@Serializable
internal data class ConversationDto(
    val id: String,
    val title: String,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant,
    val participants: List<ParticipantDto>,
    val lastMessagePreview: String? = null,
    val messageCount: Int = 0,
    val lastReadMessageIndex: Int = -1,
    val unreadMessageCount: Int = 0,
    val agent: AgentConfigurationDto = AgentConfigurationDto.None,
    val schemaVersion: Int = 3,
)

@Serializable
internal sealed class EventDto {
    abstract val id: String

    @Contextual
    abstract val timestamp: Instant
    abstract val source: ParticipantDto
}

@Serializable
internal data class UserMessageDto(
    override val id: String,
    @Contextual override val timestamp: Instant,
    override val source: ParticipantDto,
    val content: String,
    val attachments: List<AttachmentDto>,
) : EventDto()

@Serializable
internal data class AssistantMessageDto(
    override val id: String,
    @Contextual override val timestamp: Instant,
    override val source: ParticipantDto,
    val content: String,
) : EventDto()

@Serializable
internal data class AssistantProcessingDto(
    override val id: String,
    @Contextual override val timestamp: Instant,
    override val source: ParticipantDto,
    val isProcessing: Boolean,
) : EventDto()

@Serializable
internal data class ToolUseVettingDto(
    override val id: String,
    @Contextual override val timestamp: Instant,
    override val source: ParticipantDto,
    val calls: List<ToolCallDto>,
) : EventDto()

@Serializable
internal data class ToolUseNotificationDto(
    override val id: String,
    @Contextual override val timestamp: Instant,
    override val source: ParticipantDto,
    val call: ToolCallDto,
    val result: ToolCallResultDto,
) : EventDto()

@Serializable
internal sealed class ParticipantDto {
    @Serializable
    data class User(val id: String, val name: String) : ParticipantDto()

    @Serializable
    data class Agent(val id: String, val name: String) : ParticipantDto()
}

@Serializable
internal data class AttachmentDto(
    val type: String,
    val url: String,
    val name: String,
    val mimeType: String,
)

@Serializable
internal data class ToolCallDto(
    val id: String,
    val tool: String,
    val arguments: Map<String, JsonElement>,
)

@Serializable
internal sealed class ToolCallResultDto {
    @Serializable
    data class Success(val text: String) : ToolCallResultDto()

    @Serializable
    data class ExecutionFailure(val reason: String) : ToolCallResultDto()
}

@Serializable
internal sealed class AgentConfigurationDto {
    @Serializable
    data object None : AgentConfigurationDto()

    @Serializable
    data class QuestionAnswer(
        val promptName: String,
        val toolNames: List<String>,
        val modelName: String,
    ) : AgentConfigurationDto()

    @Serializable
    data class Roleplay(
        val characterName: String,
        val characterGreetingIndex: Int? = null,
        val userName: String,
        val modelName: String,
    ) : AgentConfigurationDto()
}

internal interface CardResolver {
    fun promptByName(name: String): io.github.ptitjes.konvo.core.ai.spi.PromptCard?
    fun toolByName(name: String): io.github.ptitjes.konvo.core.ai.spi.ToolCard?
    fun modelByName(name: String): Model?
    fun characterByName(name: String): CharacterCard?
}

internal object DtoMappers {
    @OptIn(ExperimentalTime::class)
    fun toDto(conv: Conversation): ConversationDto = ConversationDto(
        id = conv.id,
        title = conv.title,
        createdAt = conv.createdAt,
        updatedAt = conv.updatedAt,
        participants = conv.participants.map { toDto(it) },
        lastMessagePreview = conv.lastMessagePreview,
        messageCount = conv.messageCount,
        lastReadMessageIndex = conv.lastReadMessageIndex,
        unreadMessageCount = conv.unreadMessageCount,
        agent = when (val agentConfiguration = conv.agentConfiguration) {
            is NoAgentConfiguration -> AgentConfigurationDto.None
            is QuestionAnswerAgentConfiguration -> AgentConfigurationDto.QuestionAnswer(
                promptName = agentConfiguration.promptName,
                toolNames = agentConfiguration.toolNames,
                modelName = agentConfiguration.modelName,
            )

            is RoleplayAgentConfiguration -> AgentConfigurationDto.Roleplay(
                characterName = agentConfiguration.characterName,
                characterGreetingIndex = agentConfiguration.characterGreetingIndex,
                userName = agentConfiguration.userName,
                modelName = agentConfiguration.modelName,
            )
        },
    )

    fun fromDto(dto: ConversationDto): Conversation = Conversation(
        id = dto.id,
        title = dto.title,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt,
        participants = dto.participants.map { fromDto(it) },
        lastMessagePreview = dto.lastMessagePreview,
        messageCount = dto.messageCount,
        lastReadMessageIndex = dto.lastReadMessageIndex,
        unreadMessageCount = dto.unreadMessageCount,
        agentConfiguration = when (val agentConfigurationDto = dto.agent) {
            is AgentConfigurationDto.None -> NoAgentConfiguration
            is AgentConfigurationDto.QuestionAnswer -> QuestionAnswerAgentConfiguration(
                promptName = agentConfigurationDto.promptName,
                toolNames = agentConfigurationDto.toolNames,
                modelName = agentConfigurationDto.modelName,
            )

            is AgentConfigurationDto.Roleplay -> RoleplayAgentConfiguration(
                characterName = agentConfigurationDto.characterName,
                characterGreetingIndex = agentConfigurationDto.characterGreetingIndex,
                userName = agentConfigurationDto.userName,
                modelName = agentConfigurationDto.modelName,
            )
        },
    )

    fun toDto(p: Participant): ParticipantDto = when (p) {
        is Participant.User -> ParticipantDto.User(p.id, p.name)
        is Participant.Agent -> ParticipantDto.Agent(p.id, p.name)
    }

    fun fromDto(p: ParticipantDto): Participant = when (p) {
        is ParticipantDto.User -> Participant.User(p.id, p.name)
        is ParticipantDto.Agent -> Participant.Agent(p.id, p.name)
    }

    fun toDto(e: Event): EventDto = when (e) {
        is Event.UserMessage -> UserMessageDto(
            e.id,
            e.timestamp,
            toDto(e.source),
            e.content,
            e.attachments.map { toDto(it) })

        is Event.AssistantMessage -> AssistantMessageDto(e.id, e.timestamp, toDto(e.source), e.content)
        is Event.AssistantProcessing -> AssistantProcessingDto(e.id, e.timestamp, toDto(e.source), e.isProcessing)
        is Event.ToolUseVetting -> ToolUseVettingDto(e.id, e.timestamp, toDto(e.source), e.calls.map { toDto(it) })
        is Event.ToolUseNotification -> ToolUseNotificationDto(
            e.id,
            e.timestamp,
            toDto(e.source),
            toDto(e.call),
            toDto(e.result)
        )

        else -> error("Unsupported event type: ${e::class}")
    }

    fun fromDto(e: EventDto): Event = when (e) {
        is UserMessageDto -> Event.UserMessage(
            e.id,
            e.timestamp,
            fromDto(e.source),
            e.content,
            e.attachments.map { fromDto(it) })

        is AssistantMessageDto -> Event.AssistantMessage(e.id, e.timestamp, fromDto(e.source), e.content)
        is AssistantProcessingDto -> Event.AssistantProcessing(e.id, e.timestamp, fromDto(e.source), e.isProcessing)
        is ToolUseVettingDto -> Event.ToolUseVetting(e.id, e.timestamp, fromDto(e.source), e.calls.map { fromDto(it) })
        is ToolUseNotificationDto -> Event.ToolUseNotification(
            e.id,
            e.timestamp,
            fromDto(e.source),
            fromDto(e.call),
            fromDto(e.result)
        )
    }

    fun toDto(a: Attachment): AttachmentDto = AttachmentDto(
        type = a.type.name,
        url = a.url,
        name = a.name,
        mimeType = a.mimeType,
    )

    fun fromDto(a: AttachmentDto): Attachment = Attachment(
        type = Attachment.Type.valueOf(a.type),
        url = a.url,
        name = a.name,
        mimeType = a.mimeType,
    )

    fun toDto(c: ToolCall): ToolCallDto = ToolCallDto(
        id = c.id,
        tool = c.tool,
        arguments = c.arguments,
    )

    fun fromDto(c: ToolCallDto): ToolCall = ToolCall(
        id = c.id,
        tool = c.tool,
        arguments = c.arguments,
    )

    fun toDto(r: ToolCallResult): ToolCallResultDto = when (r) {
        is ToolCallResult.Success -> ToolCallResultDto.Success(r.text)
        is ToolCallResult.ExecutionFailure -> ToolCallResultDto.ExecutionFailure(r.reason)
    }

    fun fromDto(r: ToolCallResultDto): ToolCallResult = when (r) {
        is ToolCallResultDto.Success -> ToolCallResult.Success(r.text)
        is ToolCallResultDto.ExecutionFailure -> ToolCallResult.ExecutionFailure(r.reason)
    }
}
