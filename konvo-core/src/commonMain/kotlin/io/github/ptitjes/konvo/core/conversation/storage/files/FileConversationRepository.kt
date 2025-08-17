package io.github.ptitjes.konvo.core.conversation.storage.files

import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import io.github.ptitjes.konvo.core.platform.*
import kotlinx.coroutines.flow.*
import kotlinx.io.*
import kotlinx.io.files.*
import kotlinx.serialization.json.*
import kotlin.time.*

/**
 * File-backed implementation of ConversationRepository using Kotlinx IO and Kotlinx Serialization.
 *
 * Layout (relative to [rootPath]):
 * - conversations/index.json (ConversationIndexDto)
 * - conversations/<id>/meta.json (ConversationDto)
 * - conversations/<id>/events.ndjson (one EventDto per line)
 */
@OptIn(ExperimentalTime::class)
class FileConversationRepository(
    private val rootPath: Path,
    private val fileSystem: FileSystem = defaultFileSystem,
    private val konvo: Konvo? = null,
) : ConversationRepository {

    constructor(
        storagePaths: StoragePaths,
        fileSystem: FileSystem = defaultFileSystem,
        konvo: Konvo? = null,
    ) : this(
        rootPath = Path(storagePaths.dataDirectory, FilesLayout.CONVERSATIONS_DIR),
        fileSystem = fileSystem,
        konvo = konvo,
    )

    private val json = Json { ignoreUnknownKeys = true }

    // Internal ticker to drive flows on local mutations
    private val changeTicker = kotlinx.coroutines.flow.MutableStateFlow(0L)

    private val conversationsDir: Path get() = Path(rootPath, FilesLayout.CONVERSATIONS_DIR)
    private fun conversationDir(id: String): Path = Path(conversationsDir, id)
    private fun metaPath(id: String): Path = Path(conversationDir(id), FilesLayout.META_FILE)
    private fun eventsPath(id: String): Path = Path(conversationDir(id), FilesLayout.EVENTS_FILE)
    private val indexPath: Path get() = Path(conversationsDir, FilesLayout.INDEX_FILE)

    private fun loadIndex(): ConversationIndexDto? {
        return try {
            if (!fileSystem.exists(indexPath)) return null
            fileSystem.source(indexPath).buffered().use { src ->
                json.decodeFromString(ConversationIndexDto.serializer(), src.readString())
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun saveIndex(idx: ConversationIndexDto) {
        FileIo.atomicWrite(indexPath, fileSystem) { sink ->
            val content = json.encodeToString(ConversationIndexDto.serializer(), idx)
            sink.writeString(content)
        }
    }

    private fun rebuildIndex(): ConversationIndexDto {
        FileIo.ensureDirectoryExists(conversationsDir, fileSystem)
        val entries = mutableListOf<ConversationIndexEntryDto>()
        if (fileSystem.exists(conversationsDir)) {
            for (child in fileSystem.list(conversationsDir)) {
                val meta = Path(child, FilesLayout.META_FILE)
                if (fileSystem.exists(meta)) {
                    try {
                        fileSystem.source(meta).buffered().use { src ->
                            val dto = json.decodeFromString(ConversationDto.serializer(), src.readString())
                            entries += ConversationIndexEntryDto(
                                id = dto.id,
                                title = dto.title,
                                createdAt = dto.createdAt,
                                updatedAt = dto.updatedAt,
                                lastMessagePreview = dto.lastMessagePreview,
                                messageCount = dto.messageCount,
                                lastReadMessageIndex = dto.lastReadMessageIndex,
                                unreadMessageCount = dto.unreadMessageCount,
                            )
                        }
                    } catch (_: Throwable) {
                        // skip broken conversation
                    }
                }
            }
        }
        val idx = ConversationIndexDto(conversations = entries)
        saveIndex(idx)
        return idx
    }

    override suspend fun createConversation(initial: Conversation) {
        // Create directories
        FileIo.ensureDirectoryExists(conversationsDir, fileSystem)
        val dir = conversationDir(initial.id)
        FileIo.ensureDirectoryExists(dir, fileSystem)
        // Write meta.json atomically
        val dto = DtoMappers.toDto(initial)
        FileIo.atomicWrite(metaPath(initial.id), fileSystem) { sink ->
            val content = json.encodeToString(ConversationDto.serializer(), dto)
            sink.writeString(content)
        }
        // Ensure empty events file exists
        if (!fileSystem.exists(eventsPath(initial.id))) {
            FileIo.atomicWrite(eventsPath(initial.id), fileSystem) { sink -> sink.writeString("") }
        }
        // Update index
        val existing = loadIndex() ?: ConversationIndexDto(conversations = emptyList())
        val entry = ConversationIndexEntryDto(
            id = initial.id,
            title = initial.title,
            createdAt = initial.createdAt,
            updatedAt = initial.updatedAt,
            lastMessagePreview = initial.lastMessagePreview,
            messageCount = initial.messageCount,
            lastReadMessageIndex = initial.lastReadMessageIndex,
            unreadMessageCount = initial.unreadMessageCount,
        )
        val newIdx = existing.copy(conversations = (existing.conversations.filter { it.id != initial.id } + entry))
        saveIndex(newIdx)
        changeTicker.value = changeTicker.value + 1
    }

    private fun readConversation(id: String): Conversation? {
        val meta = metaPath(id)
        if (!fileSystem.exists(meta)) return null
        return try {
            fileSystem.source(meta).buffered().use { src ->
                val dto = json.decodeFromString(ConversationDto.serializer(), src.readString())
                DtoMappers.fromDto(dto)
            }
        } catch (_: Throwable) {
            null
        }
    }

    override fun getConversation(id: String): Flow<Conversation> =
        changeTicker.map { readConversation(id) }.onStart { emit(readConversation(id)) }.filterNotNull()
            .distinctUntilChanged()

    private fun readConversations(
        sort: Sort,
    ): List<Conversation> {
        val idx = loadIndex() ?: rebuildIndex()
        val list = idx.conversations.map { e ->
            // Participants are not part of index; load minimal Conversation without participants
            Conversation(
                id = e.id,
                title = e.title,
                createdAt = e.createdAt,
                updatedAt = e.updatedAt,
                participants = emptyList(),
                lastMessagePreview = e.lastMessagePreview,
                messageCount = e.messageCount,
                lastReadMessageIndex = e.lastReadMessageIndex,
                unreadMessageCount = e.unreadMessageCount,
            )
        }
        return when (sort) {
            is Sort.UpdatedDesc -> list.sortedByDescending { it.updatedAt }
            is Sort.UpdatedAsc -> list.sortedBy { it.updatedAt }
            is Sort.CreatedDesc -> list.sortedByDescending { it.createdAt }
            is Sort.CreatedAsc -> list.sortedBy { it.createdAt }
            is Sort.TitleAsc -> list.sortedWith(compareBy(nullsLast(String.CASE_INSENSITIVE_ORDER)) { it.title })
        }
    }

    override fun getConversations(sort: Sort): Flow<List<Conversation>> =
        changeTicker.map { readConversations(sort) }.onStart { emit(readConversations(sort)) }.distinctUntilChanged()

    override suspend fun appendEvent(conversationId: String, event: Event) {
        val metaFile = metaPath(conversationId)
        if (!fileSystem.exists(metaFile)) throw NoSuchElementException("Unknown conversation: $conversationId")
        // Append event to NDJSON by reading current content and rewriting (for portability)
        val eventsFile = eventsPath(conversationId)
        val newLine = json.encodeToString(EventDto.serializer(), DtoMappers.toDto(event)) + "\n"
        val existingContent = if (fileSystem.exists(eventsFile)) {
            fileSystem.source(eventsFile).buffered().use(Source::readString)
        } else ""
        FileIo.atomicWrite(eventsFile, fileSystem) { sink ->
            sink.writeString(existingContent)
            sink.writeString(newLine)
        }
        // Update meta and index
        val current =
            readConversation(conversationId) ?: throw NoSuchElementException("Unknown conversation: $conversationId")
        val now = Clock.System.now()
        // Compute new preview from full transcript (skip non-message events)
        val events = readEvents(conversationId)
        val newPreview = ConversationUtils.computeLastMessagePreview(events)
        val (delta, deltaUnread) = when (event) {
            is Event.UserMessage -> 1 to 1
            is Event.AssistantMessage -> 1 to 1
            else -> 0 to 0
        }
        val updated = current.copy(
            updatedAt = now,
            lastMessagePreview = newPreview,
            messageCount = current.messageCount + delta,
            unreadMessageCount = current.unreadMessageCount + deltaUnread,
        )
        // Write meta
        FileIo.atomicWrite(metaFile, fileSystem) { sink ->
            val content = json.encodeToString(ConversationDto.serializer(), DtoMappers.toDto(updated))
            sink.writeString(content)
        }
        // Update index entry
        val idx = loadIndex() ?: ConversationIndexDto(conversations = emptyList())
        val entry = ConversationIndexEntryDto(
            id = updated.id,
            title = updated.title,
            createdAt = updated.createdAt,
            updatedAt = updated.updatedAt,
            lastMessagePreview = updated.lastMessagePreview,
            messageCount = updated.messageCount,
            lastReadMessageIndex = updated.lastReadMessageIndex,
            unreadMessageCount = updated.unreadMessageCount,
        )
        saveIndex(idx.copy(conversations = idx.conversations.filter { it.id != updated.id } + entry))
        changeTicker.value = changeTicker.value + 1
    }

    override suspend fun updateConversation(conversation: Conversation) {
        val existing = readConversation(conversation.id)
            ?: throw NoSuchElementException("Unknown conversation: ${conversation.id}")
        val updated = conversation.copy(
            createdAt = existing.createdAt,
            messageCount = existing.messageCount,
            lastMessagePreview = conversation.lastMessagePreview ?: existing.lastMessagePreview,
            updatedAt = Clock.System.now(),
        )
        // Write meta
        FileIo.atomicWrite(metaPath(updated.id), fileSystem) { sink ->
            val content = json.encodeToString(ConversationDto.serializer(), DtoMappers.toDto(updated))
            sink.writeString(content)
        }
        // Update index
        val idx = loadIndex() ?: ConversationIndexDto(conversations = emptyList())
        val entry = ConversationIndexEntryDto(
            id = updated.id,
            title = updated.title,
            createdAt = updated.createdAt,
            updatedAt = updated.updatedAt,
            lastMessagePreview = updated.lastMessagePreview,
            messageCount = updated.messageCount,
            lastReadMessageIndex = updated.lastReadMessageIndex,
            unreadMessageCount = updated.unreadMessageCount,
        )
        saveIndex(idx.copy(conversations = idx.conversations.filter { it.id != updated.id } + entry))
        changeTicker.value = changeTicker.value + 1
    }

    override suspend fun deleteConversation(id: String) {
        val dir = conversationDir(id)
        if (fileSystem.exists(dir)) {
            // Delete files if present, then dir
            try {
                if (fileSystem.exists(eventsPath(id))) fileSystem.delete(eventsPath(id))
            } catch (_: Throwable) {
            }
            try {
                if (fileSystem.exists(metaPath(id))) fileSystem.delete(metaPath(id))
            } catch (_: Throwable) {
            }
            try {
                fileSystem.delete(dir)
            } catch (_: Throwable) {
            }
        }
        val idx = loadIndex() ?: ConversationIndexDto(conversations = emptyList())
        saveIndex(idx.copy(conversations = idx.conversations.filter { it.id != id }))
        changeTicker.value = changeTicker.value + 1
    }

    override suspend fun deleteAll() {
        if (fileSystem.exists(conversationsDir)) {
            for (child in fileSystem.list(conversationsDir)) {
                try {
                    val id = child.name
                    deleteConversation(id)
                } catch (_: Throwable) {
                    // continue
                }
            }
            // Remove index last
            if (fileSystem.exists(indexPath)) try {
                fileSystem.delete(indexPath)
            } catch (_: Throwable) {
            }
        }
        changeTicker.value = changeTicker.value + 1
    }

    private fun readEvents(conversationId: String): List<Event> {
        val path = eventsPath(conversationId)
        if (!fileSystem.exists(path)) return emptyList()
        val result = mutableListOf<Event>()
        fileSystem.source(path).buffered().use { src ->
            val content = src.readString()
            if (content.isEmpty()) return emptyList()
            val lines = content.split('\n')
            for (line in lines) {
                if (line.isBlank()) continue
                val evt = try {
                    val dto = json.decodeFromString(EventDto.serializer(), line)
                    DtoMappers.fromDto(dto)
                } catch (_: Throwable) {
                    continue // skip corrupt line
                }
                result.add(evt)
            }
        }
        return result
    }

    override fun getEvents(conversationId: String): Flow<List<Event>> =
        changeTicker.map { readEvents(conversationId) }.onStart { emit(readEvents(conversationId)) }
            .distinctUntilChanged()
}
