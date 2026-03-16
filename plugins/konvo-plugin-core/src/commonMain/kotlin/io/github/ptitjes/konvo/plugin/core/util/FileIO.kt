package io.github.ptitjes.konvo.plugin.core.util

import io.github.oshai.kotlinlogging.*
import kotlinx.io.*
import kotlinx.io.bytestring.*
import kotlinx.io.files.*

private val logger = KotlinLogging.logger {}

internal fun <T> FileSystem.loadFiles(
    directory: Path,
    extension: String,
    loader: FileSystem.(Path) -> T,
): List<T> = loadFiles(directory, listOf(extension), loader)

internal fun <T> FileSystem.loadFiles(
    directory: Path,
    extensions: List<String>,
    loader: FileSystem.(Path) -> T,
): List<T> {
    if (!exists(directory)) return listOf()
    return list(directory)
        .filter { it.extension in extensions }
        .mapNotNull {
            val result = runCatching { loader(it) }
            if (result.isFailure) logger.error(result.exceptionOrNull()) { "Failed to load file: $it" }
            result.getOrNull()
        }
}

internal fun FileSystem.readText(path: Path): String = source(path).buffered().use { it.readString() }

internal fun FileSystem.readBytes(path: Path): ByteString = source(path).buffered().use { it.readByteString() }

internal fun FileSystem.copy(sourcePath: Path, destinationPath: Path) {
    val destinationDirectory = destinationPath.parent!!
    if (!exists(destinationDirectory)) createDirectories(destinationDirectory)

    source(sourcePath).use { source ->
        sink(destinationPath).use { sink ->
            val buffer = Buffer()
            while (true) {
                val bytesRead = source.readAtMostTo(buffer, 8192)
                if (bytesRead == -1L) break
                sink.write(buffer, bytesRead)
            }
        }
    }
}

internal val Path.extension: String get() = name.substringAfterLast('.', "")
