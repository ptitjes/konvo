package io.github.ptitjes.konvo.core.conversations.storage.files

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.core.conversations.storage.*
import io.github.ptitjes.konvo.core.platform.*
import io.github.ptitjes.konvo.core.util.*
import kotlinx.coroutines.flow.*
import kotlinx.io.*
import kotlinx.io.files.*
import kotlinx.serialization.json.*

/**
 * File-backed implementation of ConversationRepository using Kotlinx IO and Kotlinx Serialization.
 *
 * Layout (relative to [rootPath]):
 * - conversations/index.json (ConversationIndexDto)
 * - conversations/<id>/meta.json (ConversationDto)
 * - conversations/<id>/events.ndjson (one ActionDto per line)
 */
class FileConversationRepository(
    private val rootPath: Path,
    private val fileSystem: FileSystem = defaultFileSystem,
    private val timeProvider: TimeProvider = SystemTimeProvider,
) : ConversationRepository {

    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    constructor(
        storagePaths: StoragePaths,
        fileSystem: FileSystem = defaultFileSystem,
    ) : this(
        rootPath = Path(storagePaths.dataDirectory, FilesLayout.CONVERSATIONS_DIR),
        fileSystem = fileSystem,
    )

    private val json = Json {
        ignoreUnknownKeys = true
        serializersModule = CoreActions
    }

    init {
        // Register known interaction protocols for deserialization
        InteractionProtocols.register(AgentProcessing.TurnBased)
        InteractionProtocols.register(ToolUsage.VettingProtocol)
        InteractionProtocols.register(
            InteractionProtocol(
                id = "$PLUGIN_ID/Agent#Presence",
                awaitsInput = true,
                hidesParent = false,
                reactsTo = setOf(Messaging.Message::class),
            )
        )
    }

    // Internal ticker to drive flows on local mutations
    private val changeTicker = MutableStateFlow(0L)

    private val conversationsDir: Path get() = rootPath
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

    override suspend fun create(digest: ConversationDigest) {
        // Create directories
        FileIo.ensureDirectoryExists(conversationsDir, fileSystem)
        val dir = conversationDir(digest.id)
        FileIo.ensureDirectoryExists(dir, fileSystem)
        // Write meta.json atomically
        val dto = DtoMappers.toDto(digest)
        FileIo.atomicWrite(metaPath(digest.id), fileSystem) { sink ->
            val content = json.encodeToString(ConversationDto.serializer(), dto)
            sink.writeString(content)
        }
        // Ensure empty events file exists
        if (!fileSystem.exists(eventsPath(digest.id))) {
            FileIo.atomicWrite(eventsPath(digest.id), fileSystem) { sink -> sink.writeString("") }
        }
        // Update index
        val existing = loadIndex() ?: ConversationIndexDto(conversations = emptyList())
        val entry = ConversationIndexEntryDto(
            id = digest.id,
            title = digest.title,
            createdAt = digest.createdAt,
            updatedAt = digest.updatedAt,
            lastMessagePreview = digest.lastMessagePreview,
            messageCount = digest.messageCount,
            unreadMessageCount = digest.unreadMessageCount,
        )
        val newIdx = existing.copy(conversations = (existing.conversations.filter { it.id != digest.id } + entry))
        saveIndex(newIdx)
        changeTicker.value = changeTicker.value + 1
    }

    private fun readConversation(id: String): ConversationDigest? {
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

    override fun getDigest(conversationId: String): Flow<ConversationDigest> =
        changeTicker.map { readConversation(conversationId) }
            .onStart { emit(readConversation(conversationId)) }
            .filterNotNull()
            .distinctUntilChanged()

    private fun readConversations(
        sort: Sort,
    ): List<ConversationDigest> {
        val idx = loadIndex() ?: rebuildIndex()
        val list = idx.conversations.map { e ->
            // Participants are not part of index; load minimal Conversation without participants
            ConversationDigest(
                id = e.id,
                title = e.title,
                createdAt = e.createdAt,
                updatedAt = e.updatedAt,
                participants = emptyList(),
                lastMessagePreview = e.lastMessagePreview,
                messageCount = e.messageCount,
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

    override fun getDigests(sort: Sort): Flow<List<ConversationDigest>> =
        changeTicker.map { readConversations(sort) }.onStart { emit(readConversations(sort)) }.distinctUntilChanged()

    override suspend fun appendEntry(conversationId: String, entry: ConversationEntry) {
        val metaFile = metaPath(conversationId)
        if (!fileSystem.exists(metaFile)) throw NoSuchElementException("Unknown conversation: $conversationId")

        // Serialize entry to DTO
        val dto: ConversationEntryDto = when (entry) {
            is Action<*> -> DtoMappers.toDto(entry)
            is InteractionBoundary.Start -> DtoMappers.toDto(entry)
            is InteractionBoundary.End -> DtoMappers.toDto(entry)
        }

        // Append entry to NDJSON by reading current content and rewriting (for portability)
        val eventsFile = eventsPath(conversationId)
        val newLine = json.encodeToString(ConversationEntryDto.serializer(), dto) + "\n"
        val existingContent = if (fileSystem.exists(eventsFile)) {
            fileSystem.source(eventsFile).buffered().use(Source::readString)
        } else ""
        FileIo.atomicWrite(eventsFile, fileSystem) { sink ->
            sink.writeString(existingContent)
            sink.writeString(newLine)
        }
        changeTicker.value = changeTicker.value + 1
    }

    @Deprecated("Use appendEntry instead", ReplaceWith("appendEntry(conversationId, action)"))
    override suspend fun appendAction(conversationId: String, action: Action<*>) {
        appendEntry(conversationId, action)
    }

    override suspend fun updateDigest(digest: ConversationDigest) {
        val metaFile = metaPath(digest.id)
        if (!fileSystem.exists(metaFile)) throw NoSuchElementException("Unknown conversation: ${digest.id}")
        // Write meta
        FileIo.atomicWrite(metaFile, fileSystem) { sink ->
            val content = json.encodeToString(ConversationDto.serializer(), DtoMappers.toDto(digest))
            sink.writeString(content)
        }
        // Update index
        val idx = loadIndex() ?: ConversationIndexDto(conversations = emptyList())
        val entry = ConversationIndexEntryDto(
            id = digest.id,
            title = digest.title,
            createdAt = digest.createdAt,
            updatedAt = digest.updatedAt,
            lastMessagePreview = digest.lastMessagePreview,
            messageCount = digest.messageCount,
            unreadMessageCount = digest.unreadMessageCount,
        )
        saveIndex(idx.copy(conversations = idx.conversations.filter { it.id != digest.id } + entry))
        changeTicker.value = changeTicker.value + 1
    }

    override suspend fun delete(id: String) {
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
                    delete(id)
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

    private fun readTranscript(conversationId: String): List<ConversationEntry> {
        val path = eventsPath(conversationId)
        return if (!fileSystem.exists(path)) emptyList()
        else buildList {
            val context = DeserializationContext()
            fileSystem.source(path).buffered().use { source ->
                for (line in source.readLines().filter { it.isNotBlank() }) {
                    runCatching {
                        val dto = json.decodeFromString<ConversationEntryDto>(line)
                        when (dto) {
                            is ActionDto -> DtoMappers.fromDto(dto, context)
                            is InteractionBoundaryDto.Start -> DtoMappers.fromDto(dto, context)
                            is InteractionBoundaryDto.End -> DtoMappers.fromDto(dto, context)
                        }
                    }
                        .onSuccess { add(it) }
                        .onFailure { throwable ->
                            logger.warn(throwable) { "Failed to parse conversation entry: $line" }
                        }
                }
            }
        }
    }

    @Deprecated("Use readTranscript instead")
    private fun readActions(conversationId: String): List<Action<*>> {
        return readTranscript(conversationId).filterIsInstance<Action<*>>()
    }

    override fun getTranscript(conversationId: String): Flow<ConversationTranscript> =
        combine(
            changeTicker.map { readConversation(conversationId) }.onStart { emit(readConversation(conversationId)) },
            changeTicker.map { readTranscript(conversationId) }.onStart { emit(readTranscript(conversationId)) }
        ) { digest, entries ->
            digest?.let { ConversationTranscript(it, entries) }
        }.filterNotNull().distinctUntilChanged()

    @Deprecated("Use getTranscript instead", ReplaceWith("getTranscript(conversationId)"))
    override fun getActions(conversationId: String): Flow<List<Action<*>>> =
        getTranscript(conversationId).map { it.actions }
}

private fun Source.readLines(): Sequence<String> = sequence {
    do {
        val maybeLine = readLine()?.also { yield(it) }
    } while (maybeLine != null)
}
