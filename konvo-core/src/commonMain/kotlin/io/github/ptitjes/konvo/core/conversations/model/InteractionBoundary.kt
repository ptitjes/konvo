package io.github.ptitjes.konvo.core.conversations.model

import kotlin.time.*

/**
 * Marks the start or end of an interaction in the conversation transcript.
 *
 * InteractionBoundaries are used to delimit interactions in the transcript,
 * allowing for structured grouping of related actions.
 */
sealed class InteractionBoundary(
    override val timestamp: Instant,
    override val sender: Participant,
    open val interaction: Interaction,
) : ConversationEntry(timestamp, sender) {

    /**
     * Marks the start of an interaction.
     */
    class Start internal constructor(
        override val timestamp: Instant,
        override val sender: Participant,
        override val interaction: Interaction,
    ) : InteractionBoundary(timestamp, sender, interaction)

    /**
     * Marks the end of an interaction.
     */
    class End internal constructor(
        override val timestamp: Instant,
        override val sender: Participant,
        override val interaction: Interaction,
    ) : InteractionBoundary(timestamp, sender, interaction)
}
