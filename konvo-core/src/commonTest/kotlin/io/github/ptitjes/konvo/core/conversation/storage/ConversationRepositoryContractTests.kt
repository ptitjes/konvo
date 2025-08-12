package io.github.ptitjes.konvo.core.conversation.storage

import io.github.ptitjes.konvo.core.conversation.agents.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.inmemory.*
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
        val loaded = repo.getConversation(conv.id)
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
        val updated = repo.appendEvent(conv.id, event)
        assertEquals(1, updated.messageCount)
        assertEquals("Hello world", updated.lastMessagePreview)
        val events = repo.listEvents(conv.id)
        assertEquals(1, events.size)
    }

    @Test
    fun `CRUD basics and timestamps`() = runTest {
        val repo = createRepository()
        val c1 = newConversation("c1", title = "First")
        val created = repo.createConversation(c1)
        assertEquals(c1.id, created.id)
        assertEquals(c1.createdAt, created.createdAt)
        assertEquals(c1.updatedAt, created.updatedAt)
        assertEquals(0, created.messageCount)

        val loaded = repo.getConversation("c1")
        assertNotNull(loaded)
        assertEquals("First", loaded.title)

        val none = repo.getConversation("missing")
        assertNull(none)
    }

    @Test
    fun `append updates updatedAt, lastMessagePreview and messageCount`() = runTest {
        val repo = createRepository()
        val c = repo.createConversation(newConversation("c1"))
        val beforeUpdatedAt = c.updatedAt
        val u1 = userMessage("e1", "Hello world")
        val updated1 = repo.appendEvent("c1", u1)
        assertEquals(1, updated1.messageCount)
        assertEquals("Hello world", updated1.lastMessagePreview)
        assertTrue(updated1.updatedAt >= beforeUpdatedAt)

        val a1 = assistantMessage("e2", "Hi!")
        val updated2 = repo.appendEvent("c1", a1)
        assertEquals(2, updated2.messageCount)
        assertEquals("Hi!", updated2.lastMessagePreview)
    }

    @Test
    fun `listConversations default is UpdatedDesc and supports pagination`() = runTest {
        val repo = createRepository()
        val c1 = repo.createConversation(newConversation("c1", "A"))
        // ensure different updatedAt by appending to c2 later
        val  c2 = repo.createConversation(newConversation("c2", "B"))
        // append to c2 to bump updatedAt
        repo.appendEvent("c2", userMessage("e1", "msg"))

        val listed = repo.listConversations()
        assertEquals(listOf("c2", "c1"), listed.map { it.id })

        val page1 = repo.listConversations(limit = 1, offset = 0)
        val page2 = repo.listConversations(limit = 1, offset = 1)
        assertEquals(listOf("c2"), page1.map { it.id })
        assertEquals(listOf("c1"), page2.map { it.id })
    }

    @Test
    fun `updateConversation persists title changes and updatedAt`() = runTest {
        val repo = createRepository()
        repo.createConversation(newConversation("c1", "Old"))
        val before = repo.getConversation("c1")!!.updatedAt
        val changed = repo.updateConversation(repo.getConversation("c1")!!.copy(title = "New"))
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
        assertNull(repo.getConversation("c1"))
        assertEquals(listOf("c2"), repo.listConversations().map { it.id })

        // delete all
        repo.deleteAll()
        assertTrue(repo.listConversations().isEmpty())
    }

    @Test
    fun `append to non-existent conversation fails`() = runTest {
        val repo = createRepository()
        assertFails { repo.appendEvent("missing", userMessage("e1", "nope")) }
    }

    @Test
    fun `transcript reader returns slices with from and limit`() = runTest {
        val repo = InMemoryConversationRepository()
        val c = newConversation("c1")
        repo.createConversation(c)
        // append 5 messages alternating user/assistant
        repo.appendEvent("c1", userMessage("e1", "m1"))
        repo.appendEvent("c1", assistantMessage("e2", "m2"))
        repo.appendEvent("c1", userMessage("e3", "m3"))
        repo.appendEvent("c1", assistantMessage("e4", "m4"))
        repo.appendEvent("c1", userMessage("e5", "m5"))

        val all = repo.listEvents("c1")
        assertEquals(5, all.size)

        val slice1 = repo.listEvents("c1", from = 1, limit = 2)
        assertEquals(2, slice1.size)
        // expect e2, e3 by order
        assertEquals(listOf("e2", "e3"), slice1.map { it.id })

        val sliceTail = repo.listEvents("c1", from = 4, limit = 10)
        assertEquals(listOf("e5"), sliceTail.map { it.id })
    }
}
