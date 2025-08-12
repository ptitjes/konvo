package io.github.ptitjes.konvo.frontend.compose.viewmodels

import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.test.*

class ConversationListViewModelTests {

    @OptIn(kotlin.time.ExperimentalTime::class)

    private class FakeRepo(private val list: List<Conversation>) : ConversationRepository {
        private val changesFlow = MutableSharedFlow<Unit>(replay = 1)
        override suspend fun createConversation(initial: Conversation): Conversation = initial
        override suspend fun getConversation(id: String): Conversation? = list.find { it.id == id }
        override suspend fun listConversations(sort: Sort): List<Conversation> = list
        override suspend fun appendEvent(conversationId: String, event: Event): Conversation = throw UnsupportedOperationException()
        override suspend fun updateConversation(conversation: Conversation): Conversation = conversation
        override suspend fun deleteConversation(id: String) {}
        override suspend fun deleteAll() {}
        override suspend fun listEvents(conversationId: String): List<Event> = emptyList()
        override fun changes(): Flow<Unit> = changesFlow
        fun emitChange() { runBlocking { changesFlow.emit(Unit) } }
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
        // Trigger initial load
        repo.emitChange()
        val loaded = vm.conversations.first { it.isNotEmpty() }
        assertEquals(conversations, loaded)
    }
}
