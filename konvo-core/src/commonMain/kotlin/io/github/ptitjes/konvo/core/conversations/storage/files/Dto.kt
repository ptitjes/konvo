@file:OptIn(ExperimentalTime::class)

package io.github.ptitjes.konvo.core.conversations.storage.files

import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.time.*

@Serializable
@SerialName("conversation")
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
@SerialName("event")
internal sealed class EventDto {
    abstract val id: String

    @Contextual
    abstract val timestamp: Instant
    abstract val sender: ParticipantDto
    abstract val recipients: Set<ParticipantDto>?
}

@Serializable
@SerialName("message")
internal data class MessageDto(
    override val id: String,
    @Contextual override val timestamp: Instant,
    override val sender: ParticipantDto,
    override val recipients: Set<ParticipantDto>? = null,
    val content: List<ContentPartDto>,
) : EventDto()

@Serializable
internal sealed class ContentPartDto {
    @Serializable
    @SerialName("text")
    data class Text(
        val text: String,
        val mimeType: String? = null,
    ) : ContentPartDto()

    @Serializable
    @SerialName("image")
    data class Image(
        val mimeType: String,
        val filename: String?,
        val media: AttachmentDto,
    ) : ContentPartDto()

    @Serializable
    @SerialName("video")
    data class Video(
        val mimeType: String,
        val filename: String?,
        val media: AttachmentDto,
    ) : ContentPartDto()

    @Serializable
    @SerialName("audio")
    data class Audio(
        val mimeType: String,
        val filename: String?,
        val media: AttachmentDto,
    ) : ContentPartDto()

    @Serializable
    @SerialName("file")
    data class File(
        val mimeType: String,
        val filename: String?,
        val media: AttachmentDto,
    ) : ContentPartDto()
}

@Serializable
@SerialName("tool-use-approval")
internal data class ToolUseApprovalDto(
    override val id: String,
    @Contextual override val timestamp: Instant,
    override val sender: ParticipantDto,
    override val recipients: Set<ParticipantDto>? = null,
    val approvals: List<Pair<ToolCallDto, Boolean>>,
) : EventDto()

@Serializable
@SerialName("assistant-processing")
internal data class AssistantProcessingDto(
    override val id: String,
    @Contextual override val timestamp: Instant,
    override val sender: ParticipantDto,
    override val recipients: Set<ParticipantDto>? = null,
    val isProcessing: Boolean,
) : EventDto()

@Serializable
@SerialName("tool-use-vetting")
internal data class ToolUseVettingDto(
    override val id: String,
    @Contextual override val timestamp: Instant,
    override val sender: ParticipantDto,
    override val recipients: Set<ParticipantDto>? = null,
    val calls: List<ToolCallDto>,
) : EventDto()

@Serializable
@SerialName("tool-use-notification")
internal data class ToolUseNotificationDto(
    override val id: String,
    @Contextual override val timestamp: Instant,
    override val sender: ParticipantDto,
    override val recipients: Set<ParticipantDto>? = null,
    val call: ToolCallDto,
    val result: ToolCallResultDto,
) : EventDto()

@Serializable
@SerialName("participant")
internal sealed class ParticipantDto {
    @Serializable
    @SerialName("user")
    data class User(val id: String, val name: String) : ParticipantDto()

    @Serializable
    @SerialName("agent")
    data class Agent(val id: String, val name: String) : ParticipantDto()
}

@Serializable
@SerialName("attachment")
internal data class AttachmentDto(
    val type: String,
    val url: String,
    val name: String,
    val mimeType: String,
)

@Serializable
@SerialName("tool-call")
internal data class ToolCallDto(
    val id: String,
    val tool: String,
    val arguments: Map<String, JsonElement>,
)

@Serializable
@SerialName("tool-call-result")
internal sealed class ToolCallResultDto {
    @Serializable
    @SerialName("success")
    data class Success(val text: String) : ToolCallResultDto()

    @Serializable
    @SerialName("execution-failure")
    data class ExecutionFailure(val reason: String) : ToolCallResultDto()
}

@Serializable
@SerialName("agent-configuration")
internal sealed class AgentConfigurationDto {
    @Serializable
    @SerialName("none")
    data object None : AgentConfigurationDto()

    @Serializable
    @SerialName("question-answer")
    data class QuestionAnswer(
        val mcpServerNames: Set<String>,
        val modelName: String,
    ) : AgentConfigurationDto()

    @Serializable
    @SerialName("roleplay")
    data class Roleplay(
        val characterId: String,
        val characterGreetingIndex: Int? = null,
        val personaName: String,
        val modelName: String,
        val lorebookId: String? = null,
    ) : AgentConfigurationDto()
}

internal object DtoMappers {
    fun toDto(conv: ConversationDigest): ConversationDto = ConversationDto(
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
                mcpServerNames = agentConfiguration.mcpServerNames,
                modelName = agentConfiguration.modelName,
            )

            is RoleplayAgentConfiguration -> AgentConfigurationDto.Roleplay(
                characterId = agentConfiguration.characterId,
                characterGreetingIndex = agentConfiguration.characterGreetingIndex,
                personaName = agentConfiguration.personaName,
                modelName = agentConfiguration.modelName,
                lorebookId = agentConfiguration.lorebookId,
            )
        },
    )

    fun fromDto(dto: ConversationDto): ConversationDigest = ConversationDigest(
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
                mcpServerNames = agentConfigurationDto.mcpServerNames,
                modelName = agentConfigurationDto.modelName,
            )

            is AgentConfigurationDto.Roleplay -> RoleplayAgentConfiguration(
                characterId = agentConfigurationDto.characterId,
                characterGreetingIndex = agentConfigurationDto.characterGreetingIndex,
                personaName = agentConfigurationDto.personaName,
                modelName = agentConfigurationDto.modelName,
                lorebookId = agentConfigurationDto.lorebookId,
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

    fun toDto(e: Event): EventDto = when (val details = e.payload) {
        is Event.Message -> MessageDto(
            e.id,
            e.timestamp,
            toDto(e.sender),
            e.recipients?.map { toDto(it) }?.toSet(),
            details.content.map { toDto(it) }
        )

        is Event.ToolUseApproval -> ToolUseApprovalDto(
            e.id,
            e.timestamp,
            toDto(e.sender),
            e.recipients?.map { toDto(it) }?.toSet(),
            details.approvals.map { toDto(it.key) to it.value }
        )

        is Event.AssistantProcessing -> AssistantProcessingDto(
            e.id,
            e.timestamp,
            toDto(e.sender),
            e.recipients?.map { toDto(it) }?.toSet(),
            details.isProcessing
        )

        is Event.ToolUseVetting -> ToolUseVettingDto(
            e.id,
            e.timestamp,
            toDto(e.sender),
            e.recipients?.map { toDto(it) }?.toSet(),
            details.calls.map { toDto(it) })

        is Event.ToolUseNotification -> ToolUseNotificationDto(
            e.id,
            e.timestamp,
            toDto(e.sender),
            e.recipients?.map { toDto(it) }?.toSet(),
            toDto(details.call),
            toDto(details.result)
        )

        else -> error("Unsupported event type: ${e.payload::class}")
    }

    fun fromDto(e: EventDto): Event = when (e) {
        is MessageDto -> Event(
            e.id,
            e.timestamp,
            fromDto(e.sender),
            e.recipients?.map { fromDto(it) }?.toSet(),
            Event.Message(e.content.map { fromDto(it) })
        )

        is ToolUseApprovalDto -> Event(
            e.id,
            e.timestamp,
            fromDto(e.sender),
            e.recipients?.map { fromDto(it) }?.toSet(),
            Event.ToolUseApproval(
                e.approvals.associate { fromDto(it.first) to it.second }
            )
        )

        is AssistantProcessingDto -> Event(
            e.id,
            e.timestamp,
            fromDto(e.sender),
            e.recipients?.map { fromDto(it) }?.toSet(),
            Event.AssistantProcessing(e.isProcessing)
        )

        is ToolUseVettingDto -> Event(
            e.id,
            e.timestamp,
            fromDto(e.sender),
            e.recipients?.map { fromDto(it) }?.toSet(),
            Event.ToolUseVetting(e.calls.map { fromDto(it) })
        )

        is ToolUseNotificationDto -> Event(
            e.id,
            e.timestamp,
            fromDto(e.sender),
            e.recipients?.map { fromDto(it) }?.toSet(),
            Event.ToolUseNotification(fromDto(e.call), fromDto(e.result))
        )
    }

    fun toDto(cp: ContentPart): ContentPartDto = when (cp) {
        is ContentPart.Text -> ContentPartDto.Text(cp.text, cp.mimeType)
        is ContentPart.Image -> ContentPartDto.Image(cp.mimeType, cp.filename, toDto(cp.media))
        is ContentPart.Video -> ContentPartDto.Video(cp.mimeType, cp.filename, toDto(cp.media))
        is ContentPart.Audio -> ContentPartDto.Audio(cp.mimeType, cp.filename, toDto(cp.media))
        is ContentPart.File -> ContentPartDto.File(cp.mimeType, cp.filename, toDto(cp.media))
        is ContentPart.Embed<*> -> error("Serialization of Embed is not yet supported")
    }

    fun fromDto(cp: ContentPartDto): ContentPart = when (cp) {
        is ContentPartDto.Text -> ContentPart.Text(cp.text, cp.mimeType)
        is ContentPartDto.Image -> ContentPart.Image(cp.mimeType, cp.filename, fromDto(cp.media))
        is ContentPartDto.Video -> ContentPart.Video(cp.mimeType, cp.filename, fromDto(cp.media))
        is ContentPartDto.Audio -> ContentPart.Audio(cp.mimeType, cp.filename, fromDto(cp.media))
        is ContentPartDto.File -> ContentPart.File(cp.mimeType, cp.filename, fromDto(cp.media))
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
