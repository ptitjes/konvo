package io.github.ptitjes.konvo.core

import kotlinx.io.*
import kotlinx.io.bytestring.*
import kotlinx.io.files.*

internal fun <T> FileSystem.loadFiles(
    directory: Path,
    extension: String,
    loader: FileSystem.(Path) -> T,
): List<T> {
    if (!exists(directory)) return listOf()
    return list(directory)
        .filter { it.name.endsWith(".$extension") }
        .mapNotNull { runCatching { loader(it) }.getOrNull() }
}

internal fun FileSystem.readText(path: Path): String = source(path).buffered().use { it.readString() }

internal fun FileSystem.readBytes(path: Path): ByteString = source(path).buffered().use { it.readByteString() }
