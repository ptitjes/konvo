package io.github.ptitjes.konvo.core.conversation.storage

import io.github.ptitjes.konvo.core.conversation.model.*
import kotlinx.coroutines.flow.*
import kotlin.time.*

/**
 * Repository abstraction for persisting [Conversation] metadata and full [Event] transcripts.
 *
 * Thread-safety: Implementations must be safe to call from multiple coroutines concurrently.
 * Error semantics: Methods should throw meaningful exceptions on unrecoverable errors (e.g., unknown id),
 * and never leave partial state visible to subsequent readers.
 */
@OptIn(ExperimentalTime::class)
interface ConversationRepository {

    /** Create a new conversation. The [initial] fields id/createdAt/updatedAt must be set by the caller. */
    suspend fun createConversation(initial: Conversation)

    /** Stream a conversation by id; completes if the conversation is deleted. */
    fun getConversation(id: String): Flow<Conversation>

    /**
     * Stream all conversations ordered by [sort].
     */
    fun getConversations(sort: Sort = Sort.UpdatedDesc): Flow<List<Conversation>>

    /** Append an [event] to the conversation identified by [conversationId], updating its metadata accordingly. */
    suspend fun appendEvent(conversationId: String, event: Event)

    /** Update a conversation's metadata such as title; [updatedAt] must be updated by implementation. */
    suspend fun updateConversation(conversation: Conversation)

    /** Delete a conversation and its events. */
    suspend fun deleteConversation(id: String)

    /** Delete all conversations and events. */
    suspend fun deleteAll()

    /**
     * Stream all events for a conversation, in chronological order.
     */
    fun getEvents(conversationId: String): Flow<List<Event>>
}

/** Sorting options for listing conversations. */
sealed class Sort {
    data object UpdatedDesc : Sort()
    data object UpdatedAsc : Sort()
    data object CreatedDesc : Sort()
    data object CreatedAsc : Sort()
    data object TitleAsc : Sort()
}
