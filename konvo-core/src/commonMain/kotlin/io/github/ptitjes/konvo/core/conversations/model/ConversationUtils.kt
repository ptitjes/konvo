package io.github.ptitjes.konvo.core.conversations.model

import io.github.ptitjes.konvo.core.conversations.model.events.*

/**
 * Utilities related to [ConversationDigest] derived values.
 */
object ConversationUtils {

    /**
     * Compute a last message preview from the last user or assistant message found in [events].
     * Returns null if there is no message yet.
     */
    fun computeLastMessagePreview(events: List<Event<*>>, maxLength: Int = 500): String? {
        val lastMsg = events.asReversed().firstOrNull { e ->
            e.payload is Messaging.Message
        } ?: return null

        val details = lastMsg.payload as Messaging.Message

        return details.computeMessagePreview(maxLength)
    }
}

fun Messaging.Message.computeMessagePreview(
    maxLength: Int = 500,
): String {
    val text = content.filterIsInstance<Messaging.Part.Text>().joinToString("\n") { it.text }
    return TextFormatters.truncatePreview(text, maxLength)
}
