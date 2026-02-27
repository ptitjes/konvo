package io.github.ptitjes.konvo.core.conversations.model

/**
 * Represents a conversation transcript containing the digest and all entries.
 */
class ConversationTranscript internal constructor(
    val digest: ConversationDigest,
    val entries: List<ConversationEntry>,
) {
    /**
     * Returns all actions from the transcript, filtered from all entries.
     */
    val actions: List<Action<*>>
        get() = entries.filterIsInstance<Action<*>>()
}
