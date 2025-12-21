package io.github.ptitjes.konvo.core.conversations.storage

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.coroutines.flow.*

/**
 * Repository abstraction for persisting [ConversationDigest] metadata and full [Event] transcripts.
 *
 * Thread-safety: Implementations must be safe to call from multiple coroutines concurrently.
 * Error semantics: Methods should throw meaningful exceptions on unrecoverable errors (e.g., unknown id),
 * and never leave partial state visible to subsequent readers.
 */
interface ConversationRepository {

    /**
     * Stream all conversations ordered by [sort].
     */
    fun getDigests(sort: Sort = Sort.UpdatedDesc): Flow<List<ConversationDigest>>

    /** Stream a conversation by id; completes if the conversation is deleted. */
    fun getDigest(id: String): Flow<ConversationDigest>

    /**
     * Stream all events for a conversation, in chronological order.
     */
    fun getEvents(conversationId: String): Flow<List<Event<*>>>

    /** Create a new conversation. The [initial] fields id/createdAt/updatedAt must be set by the caller. */
    suspend fun create(initial: ConversationDigest)

    /** Update a conversation's metadata such as title; [updatedAt] must be updated by implementation. */
    suspend fun updateDigest(conversation: ConversationDigest)

    /** Append an [event] to the conversation identified by [conversationId], updating its metadata accordingly. */
    suspend fun appendEvent(conversationId: String, event: Event<*>)

    /** Delete a conversation and its events. */
    suspend fun delete(id: String)

    /** Delete all conversations and events. */
    suspend fun deleteAll()
}

/** Sorting options for listing conversations. */
sealed class Sort {
    data object UpdatedDesc : Sort()
    data object UpdatedAsc : Sort()
    data object CreatedDesc : Sort()
    data object CreatedAsc : Sort()
    data object TitleAsc : Sort()
}
