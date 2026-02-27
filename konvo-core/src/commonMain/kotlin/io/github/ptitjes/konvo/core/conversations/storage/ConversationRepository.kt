package io.github.ptitjes.konvo.core.conversations.storage

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.coroutines.flow.*

/**
 * Repository abstraction for persisting [ConversationDigest] metadata and full conversation transcripts.
 *
 * Thread-safety: Implementations must be safe to call from multiple coroutines concurrently.
 * Error semantics: Methods should throw meaningful exceptions on unrecoverable errors (e.g., unknown id)
 * and never leave a partial state visible to subsequent readers.
 */
interface ConversationRepository {

    /**
     * Streams all conversations ordered by [sort].
     */
    fun getDigests(sort: Sort = Sort.UpdatedDesc): Flow<List<ConversationDigest>>

    /**
     * Streams the digest of the conversation with the given [conversationId].
     * This flow completes if the conversation is deleted.
     */
    fun getDigest(conversationId: String): Flow<ConversationDigest>

    /**
     * Streams the transcript of the conversation with the given [conversationId].
     * The transcript includes all entries (actions and interaction boundaries) in chronological order.
     */
    fun getTranscript(conversationId: String): Flow<ConversationTranscript>

    /**
     * Streams the actions of the conversation with the given [conversationId], in chronological order.
     * @deprecated Use getTranscript instead
     */
    @Deprecated("Use getTranscript instead", ReplaceWith("getTranscript(conversationId)"))
    fun getActions(conversationId: String): Flow<List<Action<*>>> =
        getTranscript(conversationId).map { it.actions }

    /**
     * Creates a new conversation with the given [digest].
     */
    suspend fun create(digest: ConversationDigest)

    /**
     * Updates the given conversation's digest.
     */
    suspend fun updateDigest(digest: ConversationDigest)

    /**
     * Appends an [entry] to the conversation with the given [conversationId].
     */
    suspend fun appendEntry(conversationId: String, entry: ConversationEntry)

    /**
     * Appends an [action] to the conversation with the given [conversationId].
     * @deprecated Use appendEntry instead
     */
    @Deprecated("Use appendEntry instead", ReplaceWith("appendEntry(conversationId, action)"))
    suspend fun appendAction(conversationId: String, action: Action<*>) =
        appendEntry(conversationId, action)

    /**
     * Deletes a conversation and its actions.
     */
    suspend fun delete(id: String)

    /**
     * Deletes all conversations and actions.
     */
    suspend fun deleteAll()
}

/**
 * Sorting options for listing conversations.
 */
sealed class Sort {
    data object UpdatedDesc : Sort()
    data object UpdatedAsc : Sort()
    data object CreatedDesc : Sort()
    data object CreatedAsc : Sort()
    data object TitleAsc : Sort()
}
