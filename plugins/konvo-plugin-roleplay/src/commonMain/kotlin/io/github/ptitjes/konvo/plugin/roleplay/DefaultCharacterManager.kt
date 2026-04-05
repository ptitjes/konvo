package io.github.ptitjes.konvo.plugin.roleplay

import io.github.ptitjes.konvo.plugin.core.platform.*
import io.github.ptitjes.konvo.plugin.core.util.*
import io.github.ptitjes.konvo.plugin.roleplay.formats.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.io.files.*
import kotlin.coroutines.*

/**
 * Character manager backed by a filesystem directory.
 */
internal class DefaultCharacterManager(
    coroutineContext: CoroutineContext,
    storagePaths: StoragePaths,
) : CharacterManager {
    private val job = SupervisorJob(coroutineContext[Job])
    private val coroutineScope = CoroutineScope(coroutineContext + job)

    override val error = MutableSharedFlow<String>()

    override val characters: SharedFlow<Set<CharacterCard>> = flow {
        val currentCharacters = query().associateBy { it.id }.toMutableMap()

        emit(currentCharacters.values.toSet())

        events.collect { event ->
            when (event) {
                is CharacterEvent.Added -> {
                    val character = event.character
                    currentCharacters += character.id to character
                    emit(currentCharacters.values.toSet())
                }

                is CharacterEvent.Deleted -> {
                    currentCharacters -= event.character.id
                    emit(currentCharacters.values.toSet())
                }
            }
        }
    }.shareIn(coroutineScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), replay = 1)

    private sealed interface CharacterEvent {
        data class Added(val character: CharacterCard) : CharacterEvent
        data class Deleted(val character: CharacterCard) : CharacterEvent
    }

    private val events = MutableSharedFlow<CharacterEvent>()

    private val path = Path(storagePaths.dataDirectory, "characters")

    private suspend fun query(): Set<CharacterCard> = withContext(Dispatchers.IO) {
        runCatching { defaultFileSystem.readFileCards(path) }
            .onFailure { error.emit(it.message ?: "Unknown error") }
            .getOrDefault(emptySet())
    }

    override fun add(sourcePath: Path) {
        coroutineScope.launch(Dispatchers.IO) {
            runCatching {
                val character = defaultFileSystem.readFileCard(sourcePath)
                defaultFileSystem.copy(sourcePath, Path(path, sourcePath.name))
                events.emit(CharacterEvent.Added(character))
            }.onFailure { error.emit(it.message ?: "Failed to add character") }
        }
    }

    override fun delete(character: CharacterCard) {
        coroutineScope.launch(Dispatchers.IO) {
            runCatching {
                defaultFileSystem.delete(Path(path, character.id))
                events.emit(CharacterEvent.Deleted(character))
            }.onFailure { error.emit(it.message ?: "Failed to delete character") }
        }
    }
}

private fun FileSystem.readFileCards(path: Path): Set<CharacterCard> =
    loadFiles(path, listOf("json", "png")) { readFileCard(it) }.toSet()

private fun FileSystem.readFileCard(path: Path): CharacterCard = when (path.extension) {
    "json" -> readJsonFileCard(path)
    "png" -> readPngFileCard(path)
    else -> error("Unknown character card format '${path.extension}'")
}

private fun FileSystem.readJsonFileCard(path: Path): CharacterCard =
    readText(path).parseCharacterCard(path.name)

private fun FileSystem.readPngFileCard(path: Path): CharacterCard =
    readBytes(path).extractCharacterCard(path.name)
