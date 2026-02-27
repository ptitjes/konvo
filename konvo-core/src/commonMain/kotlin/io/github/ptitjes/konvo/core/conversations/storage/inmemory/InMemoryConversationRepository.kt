package io.github.ptitjes.konvo.core.conversations.storage.inmemory

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.storage.*
import io.github.ptitjes.konvo.core.util.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.flow.*

/**
 * In-memory implementation of [ConversationRepository] using atomic, lock-free snapshot updates.
 */
class InMemoryConversationRepository(
    private val timeProvider: TimeProvider = SystemTimeProvider,
) : ConversationRepository {

    // Conversation id -> Conversation
    private val conversations = atomic<Map<String, ConversationDigest>>(emptyMap())

    // Conversation id -> Actions list
    private val actions = atomic<Map<String, List<Action<*>>>>(emptyMap())

    // Reactive state
    private val conversationsState = MutableStateFlow<Map<String, ConversationDigest>>(emptyMap())
    private val actionsState = MutableStateFlow<Map<String, List<Action<*>>>>(emptyMap())

    override suspend fun create(digest: ConversationDigest) {
        val newConversations = conversations.updateAndGet { prev ->
            if (prev.containsKey(digest.id)) {
                throw IllegalStateException("Conversation already exists: ${digest.id}")
            }
            prev + (digest.id to digest)
        }
        conversationsState.value = newConversations
        // Initialize empty actions list
        val newActions = actions.updateAndGet { prev -> prev + (digest.id to emptyList()) }
        actionsState.value = newActions
    }

    override fun getDigest(conversationId: String): Flow<ConversationDigest> =
        conversationsState.map { it[conversationId] }.filterNotNull().distinctUntilChanged()

    override fun getDigests(sort: Sort): Flow<List<ConversationDigest>> =
        conversationsState.map { map ->
            val list = map.values.toList()
            when (sort) {
                is Sort.UpdatedDesc -> list.sortedByDescending { it.updatedAt }
                is Sort.UpdatedAsc -> list.sortedBy { it.updatedAt }
                is Sort.CreatedDesc -> list.sortedByDescending { it.createdAt }
                is Sort.CreatedAsc -> list.sortedBy { it.createdAt }
                is Sort.TitleAsc -> list.sortedWith(compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) { it.title })
            }
        }.distinctUntilChanged()

    override suspend fun appendAction(conversationId: String, action: Action<*>) {
        // Append action
        val updatedActions = actions.updateAndGet { prev ->
            val current = prev[conversationId] ?: throw NoSuchElementException("Unknown conversation: $conversationId")
            prev + (conversationId to (current + action))
        }[conversationId]!!
        actionsState.value = actions.value
    }

    override suspend fun updateDigest(digest: ConversationDigest) {
        conversations.updateAndGet { prev ->
            if (!prev.containsKey(digest.id)) throw NoSuchElementException("Unknown conversation: ${digest.id}")
            prev + (digest.id to digest)
        }
        conversationsState.value = conversations.value
    }

    override suspend fun delete(id: String) {
        conversations.value = conversations.value - id
        actions.value = actions.value - id
        conversationsState.value = conversations.value
        actionsState.value = actions.value
    }

    override suspend fun deleteAll() {
        conversations.value = emptyMap()
        actions.value = emptyMap()
        conversationsState.value = conversations.value
        actionsState.value = actions.value
    }

    override fun getActions(conversationId: String): Flow<List<Action<*>>> =
        actionsState.map { it[conversationId] ?: emptyList() }.distinctUntilChanged()
}
