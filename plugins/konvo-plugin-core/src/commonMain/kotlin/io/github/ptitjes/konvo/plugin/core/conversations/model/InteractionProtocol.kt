package io.github.ptitjes.konvo.plugin.core.conversations.model

import kotlin.reflect.*

/**
 * Defines the protocol for an interaction.
 *
 * @property id A unique identifier for the protocol (should be URN format, e.g., "urn:konvo:io.github.ptitjes.konvo.plugin.core/Messaging#Processing")
 * @property awaitsInput Whether this interaction awaits user input
 * @property hidesParent Whether this interaction should hide its parent interaction in the UI
 * @property reactsTo Set of action payload types that this interaction reacts to
 */
data class InteractionProtocol(
    val id: String,
    val awaitsInput: Boolean,
    val hidesParent: Boolean,
    val reactsTo: Set<KClass<out Action.Payload>>,
)
