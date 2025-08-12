package io.github.ptitjes.konvo.core.conversation.model

import io.github.ptitjes.konvo.core.conversation.agents.*
import kotlin.time.*

/**
 * Conversation metadata summary used for listing and quick access in UIs.
 *
 * The full transcript (list of [Event]) is persisted and accessed via repository backends,
 * but only a summary is kept here to avoid loading all events when listing conversations.
 */
@OptIn(ExperimentalTime::class)
data class Conversation(
    val id: String,
    val title: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val participants: List<Participant>,
    val lastMessagePreview: String?,
    val messageCount: Int,
    val agentConfiguration: AgentConfiguration = NoAgentConfiguration,
)
