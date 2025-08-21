package io.github.ptitjes.konvo.core.roleplay.providers

import io.github.ptitjes.konvo.core.platform.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.core.util.*
import kotlinx.coroutines.*
import kotlinx.io.files.*
import kotlinx.serialization.json.*

class FileSystemCharacterProvider(
    storagePaths: StoragePaths,
) : CharacterProvider {
    override val name: String? = null

    private val path = Path(storagePaths.dataDirectory, "characters")

    override suspend fun query(): List<CharacterCard> = withContext(Dispatchers.IO) {
        val jsonFileCards = defaultFileSystem.jsonFileCards(path)
        val pngFileCards = defaultFileSystem.pngFileCards(path)
        (jsonFileCards + pngFileCards).sortedBy { it.name }
    }

    suspend fun add(sourcePath: Path) = withContext(Dispatchers.IO) {
        defaultFileSystem.copy(sourcePath, Path(path, sourcePath.name))
    }

    suspend fun delete(character: CharacterCard) = withContext(Dispatchers.IO) {
        defaultFileSystem.delete(Path(path, "${character.id}.json"))
    }
}

private fun FileSystem.jsonFileCards(path: Path): List<CharacterCard> =
    loadFiles(path, "json") { path ->
        val json = Json.decodeFromString<JsonObject>(readText(path))
        json.parseCharacterCard(path.name.removeSuffix(".json"))
    }

private fun FileSystem.pngFileCards(path: Path): List<CharacterCard> =
    loadFiles(path, "png") { path ->
        val bytes = readBytes(path)
        bytes.extractCharacterCard(path.name.removeSuffix(".png"))
    }
