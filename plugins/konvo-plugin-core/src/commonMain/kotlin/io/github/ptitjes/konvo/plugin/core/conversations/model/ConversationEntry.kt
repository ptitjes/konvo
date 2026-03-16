package io.github.ptitjes.konvo.plugin.core.conversations.model

import kotlin.time.*

/**
 * Represents an entry in a conversation transcript.
 * This is the base class for all items that can appear in a conversation.
 */
sealed class ConversationEntry(
    open val timestamp: Instant,
    open val sender: Participant,
)
