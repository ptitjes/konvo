package io.github.ptitjes.konvo.core.conversations.storage

import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.*
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
    ): ConversationDigest {
        return ConversationDigest(
            id = id,
            title = title,
            createdAt = timestamp,
            updatedAt = timestamp,
            participants = listOf(Participant.User("u1"), Participant.Agent("a1")),
            lastMessagePreview = null,
            messageCount = 0,
            agentConfiguration = NoAgentConfiguration,
        )
    }

    protected fun userMessage(
        id: String,
        content: String,
        timestamp: Instant,
    ): Action<*> = Action(
        id = id,
        timestamp = timestamp,
        sender = Participant.User("u1"),
        payload = Messaging.Message(
            content = listOf(Part.Text(content)),
        )
    )

    protected fun assistantMessage(
        id: String,
        content: String,
        timestamp: Instant,
    ): Action<*> = Action(
        id = id,
        timestamp = timestamp,
        sender = Participant.Agent("a1"),
        payload = Messaging.Message(
            content = listOf(Part.Text(content)),
        )
    )

    @Test
    fun `create and get conversation`() = runRepositoryTest { timeProvider, repository ->
        val conversation = newConversation(timestamp = timeProvider.now())
        repository.create(conversation)
        val loaded = repository.getDigest(conversation.id).firstOrNull()
        assertNotNull(loaded)
        assertEquals(conversation.id, loaded.id)
        assertEquals(0, loaded.messageCount)
    }

    @Test
    fun `append user message does not update preview and count`() = runRepositoryTest { timeProvider, repository ->
        val conversation = newConversation(timestamp = timeProvider.now())
        repository.create(conversation)
        val event = Action(
            id = "e1",
            timestamp = timeProvider.now(),
            sender = Participant.User("u1"),
            payload = Messaging.Message(
                content = listOf(Part.Text("Hello world")),
            )
        )
        repository.appendEntry(conversation.id, event)
        val updated = repository.getDigest(conversation.id).first()
        assertEquals(0, updated.messageCount)
        assertEquals(null, updated.lastMessagePreview)
        val transcript = repository.getTranscript(conversation.id).first()
        assertEquals(1, transcript.size)
    }

    @Test
    fun `CRUD basics and timestamps`() = runRepositoryTest { timeProvider, repository ->
        val conversation = newConversation("c1", title = "First", timeProvider.now())
        repository.create(conversation)
        val created = repository.getDigest(conversation.id).first()
        assertEquals(conversation.id, created.id)
        assertEquals(conversation.createdAt, created.createdAt)
        assertEquals(conversation.updatedAt, created.updatedAt)
        assertEquals(0, created.messageCount)

        val loaded = repository.getDigest("c1").first()
        assertEquals("First", loaded.title)
    }

    @Test
    fun `append does not update updatedAt, lastMessagePreview and messageCount`() =
        runRepositoryTest { timeProvider, repository ->
            val conversation = newConversation("c1", timestamp = timeProvider.now())
            repository.create(conversation)
            val beforeUpdatedAt = conversation.updatedAt
            val u1 = userMessage("e1", "Hello world", timeProvider.now())
            repository.appendEntry("c1", u1)
            val after1 = repository.getDigest("c1").first()
            assertEquals(0, after1.messageCount)
            assertEquals(null, after1.lastMessagePreview)
            assertEquals(beforeUpdatedAt, after1.updatedAt)

            val a1 = assistantMessage("e2", "Hi!", timeProvider.now())
            repository.appendEntry("c1", a1)
            val after2 = repository.getDigest("c1").first()
            assertEquals(0, after2.messageCount)
            assertEquals(null, after2.lastMessagePreview)
        }

    @Test
    fun `listConversations default is UpdatedDesc`() = runRepositoryTest { timeProvider, repository ->
        val c1 = newConversation("c1", "A", timeProvider.now())
        repository.create(c1)
        advanceTimeBy(1.seconds)
        val c2 = newConversation("c2", "B", timeProvider.now())
        repository.create(c2)

        val listed = repository.getDigests().first()
        assertEquals(listOf("c2", "c1"), listed.map { it.id })
    }

    @Test
    fun `updateConversation persists title changes and does not automatically update updatedAt`() = runRepositoryTest { timeProvider, repository ->
        val initial = newConversation("c1", "Old", timeProvider.now())
        repository.create(initial)
        val before = repository.getDigest("c1").first().updatedAt
        
        val updated = repository.getDigest("c1").first().copy(title = "New")
        repository.updateDigest(updated)
        
        val changed = repository.getDigest("c1").first()
        assertEquals("New", changed.title)
        assertEquals(before, changed.updatedAt)
    }

    @Test
    fun `deleteConversation removes metadata and events and deleteAll clears everything`() =
        runRepositoryTest { timeProvider, repository ->
            repository.create(newConversation("c1", timestamp = timeProvider.now()))
            repository.create(newConversation("c2", timestamp = timeProvider.now()))
            repository.appendEntry("c1", userMessage("e1", "one", timestamp = timeProvider.now()))
            repository.appendEntry("c2", userMessage("e2", "two", timestamp = timeProvider.now()))

            // delete one
            repository.delete("c1")
            assertEquals(listOf("c2"), repository.getDigests().first().map { it.id })

            // delete all
            repository.deleteAll()
            assertTrue(repository.getDigests().first().isEmpty())
        }

    @Test
    fun `append to non-existent conversation fails`() = runRepositoryTest { timeProvider, repository ->
        assertFails { repository.appendEntry("missing", userMessage("e1", "nope", timestamp = timeProvider.now())) }
    }

    @Test
    fun `transcript reader returns all events in order`() = runRepositoryTest { timeProvider, repository ->
        val c = newConversation("c1", timestamp = timeProvider.now())
        repository.create(c)
        // append 5 messages alternating user/assistant
        repository.appendEntry("c1", userMessage("e1", "m1", timestamp = timeProvider.now()))
        repository.appendEntry("c1", assistantMessage("e2", "m2", timestamp = timeProvider.now()))
        repository.appendEntry("c1", userMessage("e3", "m3", timestamp = timeProvider.now()))
        repository.appendEntry("c1", assistantMessage("e4", "m4", timestamp = timeProvider.now()))
        repository.appendEntry("c1", userMessage("e5", "m5", timestamp = timeProvider.now()))

        val transcript = repository.getTranscript("c1").first()
        assertEquals(5, transcript.size)
        assertEquals(listOf("e1", "e2", "e3", "e4", "e5"), transcript.filterIsInstance<Action<*>>().map { it.id })
    }
}
