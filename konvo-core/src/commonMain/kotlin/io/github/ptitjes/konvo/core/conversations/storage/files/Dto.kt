@file:OptIn(ExperimentalTime::class)

package io.github.ptitjes.konvo.core.conversations.storage.files

import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.serialization.*
import kotlin.time.*

@Serializable
@SerialName("conversation")
internal data class ConversationDto(
    val id: String,
    val title: String? = null,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant,
    val participants: List<ParticipantDto>,
    val lastMessagePreview: String? = null,
    val messageCount: Int = 0,
    val unreadMessageCount: Int = 0,
    val agent: AgentConfigurationDto = AgentConfigurationDto.None,
    val schemaVersion: Int = 3,
)

@Serializable
@SerialName("action")
internal data class ActionDto(
    val id: String,
    @Contextual val timestamp: Instant,
    val sender: ParticipantDto,
    val recipients: Set<ParticipantDto>? = null,
    val payload: Action.Payload,
)

@Serializable
@SerialName("participant")
internal sealed interface ParticipantDto {
    @Serializable
    @SerialName("user")
    data class User(val id: String) : ParticipantDto

    @Serializable
    @SerialName("agent")
    data class Agent(val id: String) : ParticipantDto
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
        is Participant.User -> ParticipantDto.User(p.id)
        is Participant.Agent -> ParticipantDto.Agent(p.id)
    }

    fun fromDto(p: ParticipantDto): Participant = when (p) {
        is ParticipantDto.User -> Participant.User(p.id)
        is ParticipantDto.Agent -> Participant.Agent(p.id)
    }

    fun toDto(a: Action<*>): ActionDto = ActionDto(
        a.id,
        a.timestamp,
        toDto(a.sender),
        a.recipients?.map(::toDto)?.toSet(),
        a.payload,
    )

    fun fromDto(a: ActionDto): Action<*> = Action(
        a.id,
        a.timestamp,
        fromDto(a.sender),
        a.recipients?.map(::fromDto)?.toSet(),
        a.payload,
    )
}
