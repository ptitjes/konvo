package io.github.ptitjes.konvo.frontend.compose.conversations

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.storage.*
import io.github.ptitjes.konvo.core.conversations.storage.inmemory.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

class ConversationListViewModelTests {

    @Test
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun `view model exposes conversations from repository`() = runTest {
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
        val repo: ConversationRepository = InMemoryConversationRepository()
        // Seed the in-memory repository with the expected conversations in order
        conversations.forEach { convo -> repo.createConversation(convo) }
        val vm = ConversationListViewModel(repo)
        val loaded = vm.conversations.first { it.isNotEmpty() }
        assertEquals(conversations, loaded)
    }
}
