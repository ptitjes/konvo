package io.github.ptitjes.konvo.core.conversation.storage.inmemory

import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import io.github.ptitjes.konvo.core.util.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.flow.*
import kotlin.time.*

/**
 * In-memory implementation of [ConversationRepository] using atomic, lock-free snapshot updates.
 */
@OptIn(ExperimentalTime::class)
class InMemoryConversationRepository(
    private val timeProvider: TimeProvider = SystemTimeProvider,
) : ConversationRepository {

    private val changesFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 64)

    // Conversation id -> Conversation
    private val conversations = atomic<Map<String, Conversation>>(emptyMap())

    // Conversation id -> Events list
    private val events = atomic<Map<String, List<Event>>>(emptyMap())

    override fun changes(): Flow<Unit> = changesFlow.asSharedFlow()

    override suspend fun createConversation(initial: Conversation): Conversation {
        conversations.update { prev ->
            if (prev.containsKey(initial.id)) {
                throw IllegalStateException("Conversation already exists: ${initial.id}")
            }
            prev + (initial.id to initial)
        }
        // Initialize empty events list
        events.update { prev -> prev + (initial.id to emptyList()) }
        changesFlow.tryEmit(Unit)
        return initial
    }

    override suspend fun getConversation(id: String): Conversation? = conversations.value[id]

    override suspend fun listConversations(
        sort: Sort,
        limit: Int?,
        offset: Int,
    ): List<Conversation> {
        var list = conversations.value.values.toList()
        list = when (sort) {
            is Sort.UpdatedDesc -> list.sortedByDescending { it.updatedAt }
            is Sort.UpdatedAsc -> list.sortedBy { it.updatedAt }
            is Sort.CreatedDesc -> list.sortedByDescending { it.createdAt }
            is Sort.CreatedAsc -> list.sortedBy { it.createdAt }
            is Sort.TitleAsc -> list.sortedWith(compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) { it.title })
        }
        val from = offset.coerceAtLeast(0)
        val to = if (limit == null) list.size else (from + limit).coerceAtMost(list.size)
        if (from >= list.size) return emptyList()
        return list.subList(from, to)
    }

    override suspend fun appendEvent(conversationId: String, event: Event): Conversation {
        // Append event first
        val updatedEvents = events.updateAndGet { prev ->
            val current = prev[conversationId] ?: throw NoSuchElementException("Unknown conversation: $conversationId")
            prev + (conversationId to (current + event))
        }[conversationId]!!

        // Update conversation metadata
        val updated = conversations.updateAndGet { prev ->
            val existing = prev[conversationId] ?: throw NoSuchElementException("Unknown conversation: $conversationId")
            val now = timeProvider.now()
            val (newPreview, deltaCount) = when (event) {
                is Event.UserMessage -> ConversationUtils.computeLastMessagePreview(updatedEvents) to 1
                is Event.AssistantMessage -> ConversationUtils.computeLastMessagePreview(updatedEvents) to 1
                else -> existing.lastMessagePreview to 0
            }
            val changed = existing.copy(
                updatedAt = now,
                lastMessagePreview = newPreview,
                messageCount = existing.messageCount + deltaCount,
            )
            prev + (conversationId to changed)
        }[conversationId]!!

        changesFlow.tryEmit(Unit)
        return updated
    }

    override suspend fun updateConversation(conversation: Conversation): Conversation {
        val updated = conversations.updateAndGet { prev ->
            val existing = prev[conversation.id] ?: throw NoSuchElementException("Unknown conversation: ${conversation.id}")
            val now = timeProvider.now()
            val changed = conversation.copy(
                createdAt = existing.createdAt, // preserve
                updatedAt = now,
                messageCount = existing.messageCount, // preserve unless caller intentionally changed? keep existing
                lastMessagePreview = conversation.lastMessagePreview ?: existing.lastMessagePreview,
            )
            prev + (conversation.id to changed)
        }[conversation.id]!!
        changesFlow.tryEmit(Unit)
        return updated
    }

    override suspend fun deleteConversation(id: String) {
        conversations.update { it - id }
        events.update { it - id }
        changesFlow.tryEmit(Unit)
    }

    override suspend fun deleteAll() {
        conversations.value = emptyMap()
        events.value = emptyMap()
        changesFlow.tryEmit(Unit)
    }

    override suspend fun listEvents(conversationId: String, from: Int, limit: Int?): List<Event> {
        val all = events.value[conversationId] ?: return emptyList()
        val start = from.coerceAtLeast(0).coerceAtMost(all.size)
        val end = if (limit == null) all.size else (start + limit).coerceAtMost(all.size)
        return all.subList(start, end)
    }
}
