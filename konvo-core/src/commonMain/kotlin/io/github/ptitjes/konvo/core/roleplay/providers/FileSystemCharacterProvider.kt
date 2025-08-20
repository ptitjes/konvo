package io.github.ptitjes.konvo.core.roleplay.providers

import io.github.ptitjes.konvo.core.platform.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.core.util.*
import kotlinx.io.files.*
import kotlinx.serialization.json.*

class FileSystemCharacterProvider(
    storagePaths: StoragePaths,
) : CharacterProvider {
    override val name: String? = null

    private val path = Path(storagePaths.dataDirectory, "characters")

    override suspend fun query(): List<CharacterCard> {
        val jsonFileCards = defaultFileSystem.jsonFileCards(path)
        val pngFileCards = defaultFileSystem.pngFileCards(path)
        return (jsonFileCards + pngFileCards).sortedBy { it.name }
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
