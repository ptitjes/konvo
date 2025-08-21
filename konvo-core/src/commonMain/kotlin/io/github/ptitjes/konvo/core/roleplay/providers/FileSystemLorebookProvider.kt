package io.github.ptitjes.konvo.core.roleplay.providers

import io.github.ptitjes.konvo.core.platform.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.core.util.*
import kotlinx.coroutines.*
import kotlinx.io.files.*
import kotlinx.serialization.json.*

/**
 * Simple filesystem-based lorebook provider.
 *
 * It scans the dataDirectory/lorebooks folder for JSON lorebooks.
 */
class FileSystemLorebookProvider(
    storagePaths: StoragePaths,
) : LorebookProvider {
    override val name: String? = null

    private val path = Path(storagePaths.dataDirectory, "lorebooks")

    override suspend fun query(): List<Lorebook> {
        val jsonFileCards = defaultFileSystem.jsonFileLorebooks(path)
        return jsonFileCards.sortedBy { it.name }
    }

    suspend fun add(sourcePath: Path) = withContext(Dispatchers.IO) {
        defaultFileSystem.copy(sourcePath, Path(path, sourcePath.name))
    }

    suspend fun delete(lorebook: Lorebook) = withContext(Dispatchers.IO) {
        defaultFileSystem.delete(Path(path, "${lorebook.id}.json"))
    }

    private fun FileSystem.jsonFileLorebooks(path: Path): List<Lorebook> =
        loadFiles(path, "json") { path ->
            val json = Json.decodeFromString<JsonObject>(readText(path))
            json.parseLorebook(path.name.removeSuffix(".json"))
        }
}
