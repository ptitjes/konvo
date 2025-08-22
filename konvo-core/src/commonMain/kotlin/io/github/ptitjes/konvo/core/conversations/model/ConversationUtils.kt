package io.github.ptitjes.konvo.core.conversations.model

/**
 * Utilities related to [Conversation] derived values.
 */
object ConversationUtils {

    /**
     * Compute a last message preview from the last user or assistant message found in [events].
     * Returns null if there is no message yet.
     */
    fun computeLastMessagePreview(events: List<Event>, maxLength: Int = 120): String? {
        val lastMsg = events.asReversed().firstOrNull { e ->
            when (e) {
                is Event.UserMessage -> true
                is Event.AssistantMessage -> true
                else -> false
            }
        } ?: return null
        val text = when (lastMsg) {
            is Event.UserMessage -> lastMsg.content
            is Event.AssistantMessage -> lastMsg.content
            else -> ""
        }
        val trimmed = text.trim()
        if (trimmed.length <= maxLength) return trimmed
        return trimmed.take(maxLength).trimEnd() + "\u2026"
    }
}
