package io.github.ptitjes.konvo.plugin.roleplay.providers

import io.github.ptitjes.konvo.plugin.core.platform.*
import io.github.ptitjes.konvo.plugin.core.util.*
import io.github.ptitjes.konvo.plugin.roleplay.*
import kotlinx.coroutines.*
import kotlinx.io.files.*
import kotlinx.serialization.json.*

class FileSystemCharacterProvider(
    storagePaths: StoragePaths,
) : CharacterProvider {
    override val name: String? = null

    private val path = Path(storagePaths.dataDirectory, "characters")

    override suspend fun query(): List<CharacterCard> = withContext(Dispatchers.IO) {
        defaultFileSystem.readFileCards(path).sortedBy { it.name }
    }

    suspend fun add(sourcePath: Path) = withContext(Dispatchers.IO) {
        defaultFileSystem.readFileCard(sourcePath)
        defaultFileSystem.copy(sourcePath, Path(path, sourcePath.name))
    }

    suspend fun delete(character: CharacterCard) = withContext(Dispatchers.IO) {
        defaultFileSystem.delete(Path(path, character.id))
    }
}

private fun FileSystem.readFileCards(path: Path): List<CharacterCard> =
    loadFiles(path, listOf("json", "png")) { readFileCard(it) }

private fun FileSystem.readFileCard(path: Path): CharacterCard = when (path.extension) {
    "json" -> readJsonFileCard(path)
    "png" -> readPngFileCard(path)
    else -> error("Unknown character card format")
}

private fun FileSystem.readJsonFileCard(path: Path): CharacterCard {
    val json = Json.decodeFromString<JsonObject>(readText(path))
    return json.parseCharacterCard(path.name)
}

private fun FileSystem.readPngFileCard(path: Path): CharacterCard {
    val bytes = readBytes(path)
    return bytes.extractCharacterCard(path.name)
}
