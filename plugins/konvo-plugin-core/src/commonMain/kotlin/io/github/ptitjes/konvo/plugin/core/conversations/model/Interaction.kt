package io.github.ptitjes.konvo.plugin.core.conversations.model

/**
 * Represents a scoped interaction within a conversation.
 *
 * An interaction groups together related actions that follow a specific protocol.
 * For example, a turn-based messaging interaction where the agent processes a user message
 * and responds, or a tool vetting interaction where the user approves/rejects tool calls.
 */
class Interaction internal constructor(
    val id: String,
    val protocol: InteractionProtocol,
    val parent: Interaction?,
    val trigger: Action<*>?,
) {
    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other::class != this::class) return false
        other as Interaction
        return id == other.id
    }

    override fun toString(): String = "Interaction(id='$id', protocol=${protocol.id})"
}
