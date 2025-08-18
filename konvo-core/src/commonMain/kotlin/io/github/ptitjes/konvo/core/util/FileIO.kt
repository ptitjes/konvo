package io.github.ptitjes.konvo.core.util

import io.github.oshai.kotlinlogging.*
import kotlinx.io.*
import kotlinx.io.bytestring.*
import kotlinx.io.files.*

private val logger = KotlinLogging.logger {}

internal fun <T> FileSystem.loadFiles(
    directory: Path,
    extension: String,
    loader: FileSystem.(Path) -> T,
): List<T> {
    if (!exists(directory)) return listOf()
    return list(directory)
        .filter { it.name.endsWith(".$extension") }
        .mapNotNull {
            val result = runCatching { loader(it) }
            if (result.isFailure) logger.error(result.exceptionOrNull()) { "Failed to load file: $it" }
            result.getOrNull()
        }
}

internal fun FileSystem.readText(path: Path): String = source(path).buffered().use { it.readString() }

internal fun FileSystem.readBytes(path: Path): ByteString = source(path).buffered().use { it.readByteString() }
