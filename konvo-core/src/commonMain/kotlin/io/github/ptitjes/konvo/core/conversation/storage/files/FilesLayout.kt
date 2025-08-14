@file:OptIn(ExperimentalTime::class)

package io.github.ptitjes.konvo.core.conversation.storage.files

import kotlinx.serialization.*
import kotlin.time.*

/**
 * File-system layout and serializable index for the file-backed conversation repository.
 */
object FilesLayout {
    const val CONVERSATIONS_DIR: String = "conversations"
    const val META_FILE: String = "meta.json"
    const val EVENTS_FILE: String = "events.ndjson"
    const val INDEX_FILE: String = "index.json"
}

/**
 * Top-level index containing summaries for fast listing without opening each conversation directory.
 */
@Serializable
internal data class ConversationIndexDto(
    val schemaVersion: Int = 2,
    val conversations: List<ConversationIndexEntryDto>,
)

/**
 * Summary entry mirroring [Conversation] fields necessary for listing.
 */
@Serializable
internal data class ConversationIndexEntryDto(
    val id: String,
    val title: String,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant,
    val lastMessagePreview: String?,
    val messageCount: Int,
    val lastReadMessageIndex: Int = -1,
    val unreadMessageCount: Int = 0,
)
