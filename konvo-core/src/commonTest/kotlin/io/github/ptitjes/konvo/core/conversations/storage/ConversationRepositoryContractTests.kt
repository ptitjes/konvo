package io.github.ptitjes.konvo.core.conversations.storage

import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds

/**
 * Shared contract tests for ConversationRepository implementations.
 *
 * Implementations should subclass this and provide [createRepository] and [now] if they need custom time.
 */
@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
abstract class ConversationRepositoryContractTests {

    protected abstract fun createRepository(timeProvider: TimeProvider): ConversationRepository

    private fun runRepositoryTest(testBody: suspend TestScope.(TimeProvider, ConversationRepository) -> Unit) =
        runTest {
            val timeProvider = object : TimeProvider {
                private val origin = Clock.System.now()
                private val testClock = this@runTest.testTimeSource.asClock(origin)
                override fun now(): Instant = testClock.now()
            }

            testBody(timeProvider, createRepository(timeProvider))
        }

    protected fun newConversation(
        id: String = "conversation-1",
        title: String = "Test",
        timestamp: Instant,
    ): Conversation {
        return Conversation(
            id = id,
            title = title,
            createdAt = timestamp,
            updatedAt = timestamp,
            participants = listOf(Participant.User("u1", "user"), Participant.Agent("a1", "agent")),
            lastMessagePreview = null,
            messageCount = 0,
            agentConfiguration = NoAgentConfiguration,
        )
    }

    protected fun userMessage(
        id: String,
        content: String,
        timestamp: Instant,
    ): Event.UserMessage = Event.UserMessage(
        id = id,
        timestamp = timestamp,
        source = Participant.User("u1", "user"),
        content = content,
        attachments = emptyList(),
    )

    protected fun assistantMessage(
        id: String,
        content: String,
        timestamp: Instant,
    ): Event.AssistantMessage = Event.AssistantMessage(
        id = id,
        timestamp = timestamp,
        source = Participant.Agent("a1", "agent"),
        content = content,
    )

    @Test
    fun `create and get conversation`() = runRepositoryTest { timeProvider, repository ->
        val conversation = newConversation(timestamp = timeProvider.now())
        repository.createConversation(conversation)
        val loaded = repository.getConversation(conversation.id).firstOrNull()
        assertNotNull(loaded)
        assertEquals(conversation.id, loaded.id)
        assertEquals(0, loaded.messageCount)
    }

    @Test
    fun `append user message updates preview and count`() = runRepositoryTest { timeProvider, repository ->
        val conversation = newConversation(timestamp = timeProvider.now())
        repository.createConversation(conversation)
        val event = Event.UserMessage(
            id = "e1",
            timestamp = timeProvider.now(),
            source = Participant.User("u1", "user"),
            content = "Hello world",
            attachments = emptyList()
        )
        repository.appendEvent(conversation.id, event)
        val updated = repository.getConversation(conversation.id).first()
        assertEquals(1, updated.messageCount)
        assertEquals("Hello world", updated.lastMessagePreview)
        val events = repository.getEvents(conversation.id).first()
        assertEquals(1, events.size)
    }

    @Test
    fun `CRUD basics and timestamps`() = runRepositoryTest { timeProvider, repository ->
        val conversation = newConversation("c1", title = "First", timeProvider.now())
        repository.createConversation(conversation)
        val created = repository.getConversation(conversation.id).first()
        assertEquals(conversation.id, created.id)
        assertEquals(conversation.createdAt, created.createdAt)
        assertEquals(conversation.updatedAt, created.updatedAt)
        assertEquals(0, created.messageCount)

        val loaded = repository.getConversation("c1").first()
        assertEquals("First", loaded.title)
    }

    @Test
    fun `append updates updatedAt, lastMessagePreview and messageCount`() =
        runRepositoryTest { timeProvider, repository ->
            val conversation = newConversation("c1", timestamp = timeProvider.now())
            repository.createConversation(conversation)
            val beforeUpdatedAt = conversation.updatedAt
            val u1 = userMessage("e1", "Hello world", timeProvider.now())
            repository.appendEvent("c1", u1)
            val after1 = repository.getConversation("c1").first()
            assertEquals(1, after1.messageCount)
            assertEquals("Hello world", after1.lastMessagePreview)
            assertTrue(after1.updatedAt >= beforeUpdatedAt)

            val a1 = assistantMessage("e2", "Hi!", timeProvider.now())
            repository.appendEvent("c1", a1)
            val after2 = repository.getConversation("c1").first()
            assertEquals(2, after2.messageCount)
            assertEquals("Hi!", after2.lastMessagePreview)
        }

    @Test
    fun `listConversations default is UpdatedDesc`() = runRepositoryTest { timeProvider, repository ->
        repository.createConversation(newConversation("c1", "A", timeProvider.now()))
        // ensure different updatedAt by appending to c2 later
        repository.createConversation(newConversation("c2", "B", timeProvider.now()))

        advanceTimeBy(1.seconds)
        // append to c2 to bump updatedAt
        repository.appendEvent("c2", userMessage("e1", "msg", timeProvider.now()))

        val listed = repository.getConversations().first()
        assertEquals(listOf("c2", "c1"), listed.map { it.id })
    }

    @Test
    fun `updateConversation persists title changes and updatedAt`() = runRepositoryTest { timeProvider, repository ->
        repository.createConversation(newConversation("c1", "Old", timeProvider.now()))
        val before = repository.getConversation("c1").first().updatedAt
        repository.updateConversation(repository.getConversation("c1").first().copy(title = "New"))
        val changed = repository.getConversation("c1").first()
        assertEquals("New", changed.title)
        assertTrue(changed.updatedAt >= before)
    }

    @Test
    fun `deleteConversation removes metadata and events and deleteAll clears everything`() =
        runRepositoryTest { timeProvider, repository ->
            repository.createConversation(newConversation("c1", timestamp = timeProvider.now()))
            repository.createConversation(newConversation("c2", timestamp = timeProvider.now()))
            repository.appendEvent("c1", userMessage("e1", "one", timestamp = timeProvider.now()))
            repository.appendEvent("c2", userMessage("e2", "two", timestamp = timeProvider.now()))

            // delete one
            repository.deleteConversation("c1")
            assertEquals(listOf("c2"), repository.getConversations().first().map { it.id })

            // delete all
            repository.deleteAll()
            assertTrue(repository.getConversations().first().isEmpty())
        }

    @Test
    fun `append to non-existent conversation fails`() = runRepositoryTest { timeProvider, repository ->
        assertFails { repository.appendEvent("missing", userMessage("e1", "nope", timestamp = timeProvider.now())) }
    }

    @Test
    fun `transcript reader returns all events in order`() = runRepositoryTest { timeProvider, repository ->
        val c = newConversation("c1", timestamp = timeProvider.now())
        repository.createConversation(c)
        // append 5 messages alternating user/assistant
        repository.appendEvent("c1", userMessage("e1", "m1", timestamp = timeProvider.now()))
        repository.appendEvent("c1", assistantMessage("e2", "m2", timestamp = timeProvider.now()))
        repository.appendEvent("c1", userMessage("e3", "m3", timestamp = timeProvider.now()))
        repository.appendEvent("c1", assistantMessage("e4", "m4", timestamp = timeProvider.now()))
        repository.appendEvent("c1", userMessage("e5", "m5", timestamp = timeProvider.now()))

        val all = repository.getEvents("c1").first()
        assertEquals(5, all.size)
        assertEquals(listOf("e1", "e2", "e3", "e4", "e5"), all.map { it.id })
    }
}
