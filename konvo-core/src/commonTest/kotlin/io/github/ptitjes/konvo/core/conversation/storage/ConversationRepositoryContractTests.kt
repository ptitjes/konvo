package io.github.ptitjes.konvo.core.conversation.storage

import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.inmemory.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*
import kotlin.time.*

/**
 * Shared contract tests for ConversationRepository implementations.
 *
 * Implementations should subclass this and provide [createRepository] and [now] if they need custom time.
 */
@OptIn(ExperimentalTime::class)
abstract class ConversationRepositoryContractTests {

    protected abstract fun createRepository(): ConversationRepository

    protected open fun now(): Instant = Clock.System.now()

    protected fun newConversation(id: String = "conv-1", title: String = "Test"): Conversation {
        val t = now()
        return Conversation(
            id = id,
            title = title,
            createdAt = t,
            updatedAt = t,
            participants = listOf(Participant.User("u1", "user"), Participant.Agent("a1", "agent")),
            lastMessagePreview = null,
            messageCount = 0,
            agentConfiguration = NoAgentConfiguration,
        )
    }

    protected fun userMessage(
        id: String,
        content: String,
        ts: Instant = now(),
    ): Event.UserMessage = Event.UserMessage(
        id = id,
        timestamp = ts,
        source = Participant.User("u1", "user"),
        content = content,
        attachments = emptyList(),
    )

    protected fun assistantMessage(
        id: String,
        content: String,
        ts: Instant = now(),
    ): Event.AssistantMessage = Event.AssistantMessage(
        id = id,
        timestamp = ts,
        source = Participant.Agent("a1", "agent"),
        content = content,
    )

    @Test
    fun `create and get conversation`() = runTest {
        val repo = createRepository()
        val conv = newConversation()
        repo.createConversation(conv)
        val loaded = repo.getConversation(conv.id).firstOrNull()
        assertNotNull(loaded)
        assertEquals(conv.id, loaded.id)
        assertEquals(0, loaded.messageCount)
    }

    @Test
    fun `append user message updates preview and count`() = runTest {
        val repo = createRepository()
        val conv = newConversation()
        repo.createConversation(conv)
        val event = Event.UserMessage(
            id = "e1",
            timestamp = Clock.System.now(),
            source = Participant.User("u1", "user"),
            content = "Hello world",
            attachments = emptyList()
        )
        repo.appendEvent(conv.id, event)
        val updated = repo.getConversation(conv.id).first()
        assertEquals(1, updated.messageCount)
        assertEquals("Hello world", updated.lastMessagePreview)
        val events = repo.getEvents(conv.id).first()
        assertEquals(1, events.size)
    }

    @Test
    fun `CRUD basics and timestamps`() = runTest {
        val repo = createRepository()
        val c1 = newConversation("c1", title = "First")
        repo.createConversation(c1)
        val created = repo.getConversation(c1.id).first()
        assertEquals(c1.id, created.id)
        assertEquals(c1.createdAt, created.createdAt)
        assertEquals(c1.updatedAt, created.updatedAt)
        assertEquals(0, created.messageCount)

        val loaded = repo.getConversation("c1").first()
        assertEquals("First", loaded.title)
    }

    @Test
    fun `append updates updatedAt, lastMessagePreview and messageCount`() = runTest {
        val repo = createRepository()
        val c = newConversation("c1")
        repo.createConversation(c)
        val beforeUpdatedAt = c.updatedAt
        val u1 = userMessage("e1", "Hello world")
        repo.appendEvent("c1", u1)
        val after1 = repo.getConversation("c1").first()
        assertEquals(1, after1.messageCount)
        assertEquals("Hello world", after1.lastMessagePreview)
        assertTrue(after1.updatedAt >= beforeUpdatedAt)

        val a1 = assistantMessage("e2", "Hi!")
        repo.appendEvent("c1", a1)
        val after2 = repo.getConversation("c1").first()
        assertEquals(2, after2.messageCount)
        assertEquals("Hi!", after2.lastMessagePreview)
    }

    @Test
    fun `listConversations default is UpdatedDesc`() = runTest {
        val repo = createRepository()
        repo.createConversation(newConversation("c1", "A"))
        // ensure different updatedAt by appending to c2 later
        repo.createConversation(newConversation("c2", "B"))
        // append to c2 to bump updatedAt
        repo.appendEvent("c2", userMessage("e1", "msg"))

        val listed = repo.getConversations().first()
        assertEquals(listOf("c2", "c1"), listed.map { it.id })
    }

    @Test
    fun `updateConversation persists title changes and updatedAt`() = runTest {
        val repo = createRepository()
        repo.createConversation(newConversation("c1", "Old"))
        val before = repo.getConversation("c1").first().updatedAt
        repo.updateConversation(repo.getConversation("c1").first().copy(title = "New"))
        val changed = repo.getConversation("c1").first()
        assertEquals("New", changed.title)
        assertTrue(changed.updatedAt >= before)
    }

    @Test
    fun `deleteConversation removes metadata and events and deleteAll clears everything`() = runTest {
        val repo = createRepository()
        repo.createConversation(newConversation("c1"))
        repo.createConversation(newConversation("c2"))
        repo.appendEvent("c1", userMessage("e1", "one"))
        repo.appendEvent("c2", userMessage("e2", "two"))

        // delete one
        repo.deleteConversation("c1")
        assertEquals(listOf("c2"), repo.getConversations().first().map { it.id })

        // delete all
        repo.deleteAll()
        assertTrue(repo.getConversations().first().isEmpty())
    }

    @Test
    fun `append to non-existent conversation fails`() = runTest {
        val repo = createRepository()
        assertFails { repo.appendEvent("missing", userMessage("e1", "nope")) }
    }

    @Test
    fun `transcript reader returns all events in order`() = runTest {
        val repo = InMemoryConversationRepository()
        val c = newConversation("c1")
        repo.createConversation(c)
        // append 5 messages alternating user/assistant
        repo.appendEvent("c1", userMessage("e1", "m1"))
        repo.appendEvent("c1", assistantMessage("e2", "m2"))
        repo.appendEvent("c1", userMessage("e3", "m3"))
        repo.appendEvent("c1", assistantMessage("e4", "m4"))
        repo.appendEvent("c1", userMessage("e5", "m5"))

        val all = repo.getEvents("c1").first()
        assertEquals(5, all.size)
        assertEquals(listOf("e1","e2","e3","e4","e5"), all.map { it.id })
    }
}
