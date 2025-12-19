package io.github.ptitjes.konvo.core.conversations.model

/**
 * Utilities related to [ConversationDigest] derived values.
 */
object ConversationUtils {

    /**
     * Compute a last message preview from the last user or assistant message found in [events].
     * Returns null if there is no message yet.
     */
    fun computeLastMessagePreview(events: List<Event>, maxLength: Int = 500): String? {
        val lastMsg = events.asReversed().firstOrNull { e ->
            e.payload is Event.Message
        } ?: return null

        val details = lastMsg.payload as Event.Message
        val text = details.content.filterIsInstance<ContentPart.Text>().joinToString("\n") { it.text }

        return TextFormatters.truncatePreview(text, maxLength)
    }
}
