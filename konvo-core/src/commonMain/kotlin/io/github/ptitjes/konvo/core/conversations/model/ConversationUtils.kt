package io.github.ptitjes.konvo.core.conversations.model

import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.Part

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
        val text = details.content.filterIsInstance<Part.Text>().joinToString("\n") { it.text }

        return TextFormatters.truncatePreview(text, maxLength)
    }
}
