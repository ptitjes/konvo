@file:OptIn(ExperimentalTime::class)

package io.github.ptitjes.konvo.plugin.core.conversations.storage.files

import io.github.ptitjes.konvo.plugin.core.agents.*
import io.github.ptitjes.konvo.plugin.core.conversations.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
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
    val agentConfiguration: @Contextual AgentConfiguration?,
    val schemaVersion: Int = 4, // Schema 4: Added InteractionBoundary serialization support
)

@Serializable
internal sealed interface ConversationEntryDto

@Serializable
@SerialName("action")
internal data class ActionDto(
    val id: String,
    @Contextual val timestamp: Instant,
    val sender: ParticipantDto,
    val recipients: Set<ParticipantDto>? = null,
    val payload: Action.Payload,
    val interactionId: String? = null,
) : ConversationEntryDto

@Serializable
internal sealed interface InteractionBoundaryDto : ConversationEntryDto {
    val interactionId: String

    @Serializable
    @SerialName("interaction-boundary-start")
    data class Start(
        @Contextual val timestamp: Instant,
        val sender: ParticipantDto,
        override val interactionId: String,
        val protocolId: String,
        val parentInteractionId: String?,
        val triggerActionId: String?,
    ) : InteractionBoundaryDto

    @Serializable
    @SerialName("interaction-boundary-end")
    data class End(
        @Contextual val timestamp: Instant,
        val sender: ParticipantDto,
        override val interactionId: String,
    ) : InteractionBoundaryDto
}

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

internal class SerializationContext(
    private val interactionProtocolRegistry: InteractionProtocolRegistry,
) {
    fun getProtocolId(protocol: InteractionProtocol): String {
        return interactionProtocolRegistry.idByProtocol.getValue(protocol)
    }
}

internal class DeserializationContext(
    private val interactionProtocolRegistry: InteractionProtocolRegistry,
) {
    private val interactions = mutableMapOf<String, Interaction>()
    private val actions = mutableMapOf<String, Action<*>>()

    fun getProtocol(id: String): InteractionProtocol {
        return interactionProtocolRegistry.protocolsById.getValue(id)
    }

    fun getOrCreateInteraction(
        id: String,
        protocol: InteractionProtocol,
        parent: Interaction?,
        trigger: Action<*>?,
    ): Interaction {
        return interactions.getOrPut(id) {
            Interaction(id, protocol, parent, trigger)
        }
    }

    fun addAction(action: Action<*>) {
        actions[action.id] = action
    }

    fun getAction(id: String): Action<*>? = actions[id]

    fun getInteraction(id: String): Interaction? = interactions[id]
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
        agentConfiguration = conv.agentConfiguration,
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
        agentConfiguration = dto.agentConfiguration,
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
        a.interaction?.id,
    )

    fun fromDto(a: ActionDto, context: DeserializationContext): Action<*> {
        val interaction = a.interactionId?.let { context.getInteraction(it) }
        return Action(
            a.id,
            a.timestamp,
            fromDto(a.sender),
            a.recipients?.map(::fromDto)?.toSet(),
            interaction,
            a.payload,
        ).also { context.addAction(it) }
    }

    @Deprecated("Use fromDto with DeserializationContext instead")
    fun fromDto(a: ActionDto): Action<*> = Action(
        a.id,
        a.timestamp,
        fromDto(a.sender),
        a.recipients?.map(::fromDto)?.toSet(),
        payload = a.payload,
    )

    fun toDto(boundary: InteractionBoundary.Start, context: SerializationContext): InteractionBoundaryDto.Start =
        InteractionBoundaryDto.Start(
            timestamp = boundary.timestamp,
            sender = toDto(boundary.sender),
            interactionId = boundary.interaction.id,
            protocolId = context.getProtocolId(boundary.interaction.protocol),
            parentInteractionId = boundary.interaction.parent?.id,
            triggerActionId = boundary.interaction.trigger?.id,
        )

    fun toDto(boundary: InteractionBoundary.End): InteractionBoundaryDto.End = InteractionBoundaryDto.End(
        timestamp = boundary.timestamp,
        sender = toDto(boundary.sender),
        interactionId = boundary.interaction.id,
    )

    fun fromDto(dto: InteractionBoundaryDto.Start, context: DeserializationContext): InteractionBoundary.Start {
        val protocol = context.getProtocol(dto.protocolId)
        val parent = dto.parentInteractionId?.let { context.getInteraction(it) }
        val trigger = dto.triggerActionId?.let { context.getAction(it) }
        val interaction = context.getOrCreateInteraction(dto.interactionId, protocol, parent, trigger)
        return InteractionBoundary.Start(
            timestamp = dto.timestamp,
            sender = fromDto(dto.sender),
            interaction = interaction,
        )
    }

    fun fromDto(dto: InteractionBoundaryDto.End, context: DeserializationContext): InteractionBoundary.End {
        val interaction = context.getInteraction(dto.interactionId)
            ?: throw IllegalStateException("Unknown interaction: ${dto.interactionId}")
        return InteractionBoundary.End(
            timestamp = dto.timestamp,
            sender = fromDto(dto.sender),
            interaction = interaction,
        )
    }
}
