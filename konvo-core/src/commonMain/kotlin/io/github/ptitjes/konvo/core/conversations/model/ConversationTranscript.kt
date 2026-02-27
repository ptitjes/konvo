package io.github.ptitjes.konvo.core.conversations.model

/**
 * Represents a conversation transcript containing the digest and all entries.
 * This class implements List<ConversationEntry> by delegating to the entries property.
 */
class ConversationTranscript internal constructor(
    val digest: ConversationDigest,
    val entries: List<ConversationEntry>,
) : List<ConversationEntry> by entries
