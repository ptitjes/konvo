package io.github.ptitjes.konvo.frontend.compose.viewmodels

import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.test.*

class ConversationListViewModelTests {

    @OptIn(kotlin.time.ExperimentalTime::class)

    private class FakeRepo(private val list: List<Conversation>) : ConversationRepository {
        private val conversations = MutableStateFlow(list)
        override suspend fun createConversation(initial: Conversation): Conversation = initial
        override fun getConversation(id: String): Flow<Conversation> = conversations.map { l -> l.first { it.id == id } }
        override fun getConversations(sort: Sort): Flow<List<Conversation>> = conversations
        override suspend fun appendEvent(conversationId: String, event: Event): Conversation = throw UnsupportedOperationException()
        override suspend fun updateConversation(conversation: Conversation): Conversation = conversation
        override suspend fun deleteConversation(id: String) {}
        override suspend fun deleteAll() {}
        override fun getEvents(conversationId: String): Flow<List<Event>> = MutableStateFlow(emptyList())
    }

    @Test
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun `view model exposes conversations from repository`() = runBlocking {
        val conversations = listOf(
            Conversation(
                id = "1",
                title = "First",
                createdAt = kotlin.time.Instant.fromEpochMilliseconds(0),
                updatedAt = kotlin.time.Instant.fromEpochMilliseconds(0),
                participants = emptyList<Participant>(),
                lastMessagePreview = "Hello",
                messageCount = 1,
            ),
            Conversation(
                id = "2",
                title = "Test conversation",
                createdAt = kotlin.time.Instant.fromEpochMilliseconds(0),
                updatedAt = kotlin.time.Instant.fromEpochMilliseconds(0),
                participants = emptyList(),
                lastMessagePreview = "World",
                messageCount = 2,
            ),
        )
        val repo = FakeRepo(conversations)
        val vm = ConversationListViewModel(repo)
        val loaded = vm.conversations.first { it.isNotEmpty() }
        assertEquals(conversations, loaded)
    }
}
