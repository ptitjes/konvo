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

    // Conversation id -> Conversation
    private val conversations = atomic<Map<String, Conversation>>(emptyMap())

    // Conversation id -> Events list
    private val events = atomic<Map<String, List<Event>>>(emptyMap())

    // Reactive state
    private val conversationsState = MutableStateFlow<Map<String, Conversation>>(emptyMap())
    private val eventsState = MutableStateFlow<Map<String, List<Event>>>(emptyMap())

    override suspend fun createConversation(initial: Conversation) {
        val newConversations = conversations.updateAndGet { prev ->
            if (prev.containsKey(initial.id)) {
                throw IllegalStateException("Conversation already exists: ${initial.id}")
            }
            prev + (initial.id to initial)
        }
        conversationsState.value = newConversations
        // Initialize empty events list
        val newEvents = events.updateAndGet { prev -> prev + (initial.id to emptyList()) }
        eventsState.value = newEvents
    }

    override fun getConversation(id: String): Flow<Conversation> =
        conversationsState.map { it[id] }.filterNotNull().distinctUntilChanged()

    override fun getConversations(sort: Sort): Flow<List<Conversation>> =
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

    override suspend fun appendEvent(conversationId: String, event: Event) {
        // Append event first
        val updatedEvents = events.updateAndGet { prev ->
            val current = prev[conversationId] ?: throw NoSuchElementException("Unknown conversation: $conversationId")
            prev + (conversationId to (current + event))
        }[conversationId]!!
        eventsState.value = events.value

        // Update conversation metadata
        val updated = conversations.updateAndGet { prev ->
            val existing = prev[conversationId] ?: throw NoSuchElementException("Unknown conversation: $conversationId")
            val now = timeProvider.now()
            val (newPreview, deltaCount, deltaUnread) = when (event) {
                is Event.UserMessage -> Triple(ConversationUtils.computeLastMessagePreview(updatedEvents), 1, 1)
                is Event.AssistantMessage -> Triple(ConversationUtils.computeLastMessagePreview(updatedEvents), 1, 1)
                else -> Triple(existing.lastMessagePreview, 0, 0)
            }
            val changed = existing.copy(
                updatedAt = now,
                lastMessagePreview = newPreview,
                messageCount = existing.messageCount + deltaCount,
                unreadMessageCount = existing.unreadMessageCount + deltaUnread,
            )
            prev + (conversationId to changed)
        }[conversationId]!!
        conversationsState.value = conversations.value
    }

    override suspend fun updateConversation(conversation: Conversation) {
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
        conversationsState.value = conversations.value
    }

    override suspend fun deleteConversation(id: String) {
        conversations.value = conversations.value - id
        events.value = events.value - id
        conversationsState.value = conversations.value
        eventsState.value = events.value
    }

    override suspend fun deleteAll() {
        conversations.value = emptyMap()
        events.value = emptyMap()
        conversationsState.value = conversations.value
        eventsState.value = events.value
    }

    override fun getEvents(conversationId: String): Flow<List<Event>> =
        eventsState.map { it[conversationId] ?: emptyList() }.distinctUntilChanged()
}
