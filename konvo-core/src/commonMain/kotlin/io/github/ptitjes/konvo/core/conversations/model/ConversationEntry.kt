package io.github.ptitjes.konvo.core.conversations.model

import kotlin.time.*

/**
 * Represents an entry in a conversation transcript.
 * This is the base class for all items that can appear in a conversation.
 */
sealed class ConversationEntry protected constructor(
    open val timestamp: Instant,
    open val sender: Participant,
)
