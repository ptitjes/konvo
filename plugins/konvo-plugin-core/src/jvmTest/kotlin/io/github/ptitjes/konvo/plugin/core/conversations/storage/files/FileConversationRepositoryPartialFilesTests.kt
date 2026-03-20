@file:OptIn(ExperimentalTime::class)

package io.github.ptitjes.konvo.plugin.core.conversations.storage.files

import io.github.ptitjes.konvo.plugin.core.conversations.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.events.Messaging.*
import io.github.ptitjes.konvo.plugin.core.platform.*
import io.github.ptitjes.syrup.specification.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlinx.io.*
import kotlinx.io.files.*
import kotlinx.io.files.Path
import org.kodein.di.*
import kotlin.io.path.createTempDirectory
import kotlin.reflect.*
import kotlin.test.*
import kotlin.time.*

class FileConversationRepositoryPartialFilesTests {

    private fun newConversation(
        id: String = "c1",
        title: String = "Test",
        now: Instant = Clock.System.now(),
    ): ConversationDigest =
        ConversationDigest(
            id = id,
            title = title,
            createdAt = now,
            updatedAt = now,
            participants = listOf(Participant.User("u1"), Participant.Agent("a1")),
        )

    private fun userMessage(id: String, content: String, ts: Instant = Clock.System.now()): Action<*> =
        Action(
            id = id,
            timestamp = ts,
            sender = Participant.User("u1"),
            payload = Message(
                content = listOf(Part.Text(content)),
            )
        )

    @Test
    fun `listEvents skips truncated NDJSON line and degrades gracefully`() = runTest {
        // Arrange: repository in temp dir
        val tmp = createTempDirectory("konvo-file-repo-")
        val root = Path(tmp.toString())

        val storagePaths = object : StoragePaths {
            override val configDirectory: Path = Path(root, "config")
            override val dataDirectory: Path = Path(root, "data")
            override val cacheDirectory: Path = Path(root, "cache")
        }

        val context = object : MockPluginContext() {
            override fun <T : Any> contributions(extensionPoint: ExtensionPoint.Plural<T>): LazyDelegate<Set<T>> =
                object : LazyDelegate<Set<T>> {
                    override fun provideDelegate(receiver: Any?, prop: KProperty<Any?>): Lazy<Set<T>> =
                        lazy { emptySet() }
                }
        }

        val actionRegistry = DefaultActionRegistry(context)
        val interactionProtocolRegistry = DefaultInteractionProtocolRegistry(context)

        val repo = FileConversationRepository(
            storagePaths = storagePaths,
            actionRegistry = actionRegistry,
            interactionProtocolRegistry = interactionProtocolRegistry,
            pluginContext = context,
        )

        // Create conversation and append two valid events
        val conversation = newConversation()
        repo.create(conversation)
        repo.appendEntry(conversation.id, userMessage("e1", "Hello"))
        repo.appendEntry(conversation.id, userMessage("e2", "World"))

        // Sanity: we should have two events
        val before = repo.getTranscript(conversation.id).first()
        assertEquals(2, before.entries.size)

        // Corrupt the events file by appending a truncated JSON line (no newline)
        val conversationsDir = Path(root, FilesLayout.CONVERSATIONS_DIR)
        val eventsPath = Path(Path(conversationsDir, conversation.id), FilesLayout.EVENTS_FILE)
        val fs: FileSystem = defaultFileSystem

        // Read existing content
        val existing = if (fs.exists(eventsPath)) {
            fs.source(eventsPath).buffered().use(Source::readString)
        } else ""

        // Append a broken line
        FileIo.atomicWrite(eventsPath, fs) { sink ->
            sink.writeString(existing)
            sink.writeString("{this is not valid json")
            // note: no trailing newline
        }

        // Act: list events should skip the bad line and still return the two valid ones
        val transcript = repo.getTranscript(conversation.id).first()

        // Assert
        assertEquals(2, transcript.entries.size)
        val actions = transcript.actions
        assertTrue(actions.all { it.payload is Message })
        assertEquals(listOf("e1", "e2"), actions.map { it.id })
    }
}
