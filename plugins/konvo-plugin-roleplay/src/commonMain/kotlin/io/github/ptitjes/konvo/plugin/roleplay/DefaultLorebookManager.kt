package io.github.ptitjes.konvo.plugin.roleplay

import io.github.ptitjes.konvo.plugin.core.platform.*
import io.github.ptitjes.konvo.plugin.core.util.*
import io.github.ptitjes.konvo.plugin.roleplay.formats.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.io.files.*
import kotlin.coroutines.*

/**
 * Lorebook manager backed by a filesystem directory.
 */
internal class DefaultLorebookManager(
    coroutineContext: CoroutineContext,
    storagePaths: StoragePaths,
) : LorebookManager {
    private val job = SupervisorJob(coroutineContext[Job])
    private val coroutineScope = CoroutineScope(coroutineContext + job)

    override val error = MutableSharedFlow<String>()

    override val lorebooks: SharedFlow<Set<Lorebook>> = flow {
        val currentLorebooks = query().associateBy { it.id }.toMutableMap()

        emit(currentLorebooks.values.toSet())

        events.collect { event ->
            when (event) {
                is LorebookEvent.Added -> {
                    val lorebook = event.lorebook
                    currentLorebooks += lorebook.id to lorebook
                    emit(currentLorebooks.values.toSet())
                }

                is LorebookEvent.Deleted -> {
                    currentLorebooks -= event.lorebook.id
                    emit(currentLorebooks.values.toSet())
                }
            }
        }
    }.shareIn(coroutineScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), replay = 1)

    private sealed interface LorebookEvent {
        data class Added(val lorebook: Lorebook) : LorebookEvent
        data class Deleted(val lorebook: Lorebook) : LorebookEvent
    }

    private val events = MutableSharedFlow<LorebookEvent>()

    private val path = Path(storagePaths.dataDirectory, "lorebooks")

    private suspend fun query(): List<Lorebook> = withContext(Dispatchers.IO) {
        runCatching { defaultFileSystem.readFileLorebooks(path) }
            .onFailure { error.emit(it.message ?: "Unknown error") }
            .getOrDefault(emptyList())
    }

    override fun add(sourcePath: Path) {
        coroutineScope.launch(Dispatchers.IO) {
            runCatching {
                val lorebook = defaultFileSystem.readJsonFileLorebook(sourcePath)
                defaultFileSystem.copy(sourcePath, Path(path, sourcePath.name))
                events.emit(LorebookEvent.Added(lorebook))
            }.onFailure { error.emit(it.message ?: "Failed to add lorebook") }
        }
    }

    override fun delete(lorebook: Lorebook) {
        coroutineScope.launch(Dispatchers.IO) {
            runCatching {
                defaultFileSystem.delete(Path(path, lorebook.id!!))
                events.emit(LorebookEvent.Deleted(lorebook))
            }.onFailure { error.emit(it.message ?: "Failed to delete lorebook") }
        }
    }
}

private fun FileSystem.readFileLorebooks(path: Path): List<Lorebook> =
    loadFiles(path, "json") { path -> readJsonFileLorebook(path) }

private fun FileSystem.readJsonFileLorebook(path: Path): Lorebook =
    readText(path).parseLorebook(path.name)
