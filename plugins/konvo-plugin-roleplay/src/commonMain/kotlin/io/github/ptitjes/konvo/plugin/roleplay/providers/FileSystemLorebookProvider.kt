package io.github.ptitjes.konvo.plugin.roleplay.providers

import io.github.ptitjes.konvo.plugin.core.platform.*
import io.github.ptitjes.konvo.plugin.core.util.*
import io.github.ptitjes.konvo.plugin.roleplay.*
import io.github.ptitjes.konvo.plugin.roleplay.formats.*
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

    override suspend fun query(): List<Lorebook> = withContext(Dispatchers.IO) {
        defaultFileSystem.jsonFileLorebooks(path).sortedBy { it.name }
    }

    suspend fun add(sourcePath: Path) = withContext(Dispatchers.IO) {
        defaultFileSystem.readJsonFileLorebook(sourcePath)
        defaultFileSystem.copy(sourcePath, Path(path, sourcePath.name))
    }

    suspend fun delete(lorebook: Lorebook) = withContext(Dispatchers.IO) {
        defaultFileSystem.delete(Path(path, lorebook.id!!))
    }
}

private fun FileSystem.jsonFileLorebooks(path: Path): List<Lorebook> =
    loadFiles(path, "json") { path ->
        readJsonFileLorebook(path)
    }

private fun FileSystem.readJsonFileLorebook(path: Path): Lorebook {
    val json = Json.decodeFromString<JsonObject>(readText(path))
    return json.parseLorebook(path.name)
}
