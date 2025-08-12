package io.github.ptitjes.konvo.core.conversation.storage.files

import io.github.ptitjes.konvo.core.*
import kotlinx.io.*
import kotlinx.io.files.*

/**
 * Small File IO utilities for the file-backed conversation repository.
 */
internal object FileIo {

    /** Ensure that the given directory [directory] exists, creating it (and parents) when necessary. */
    fun ensureDirectoryExists(directory: Path, fileSystem: FileSystem = defaultFileSystem) {
        fileSystem.createDirectories(directory)
    }

    /**
     * Write content atomically by writing to a temp file and then renaming to [destination].
     * The parent directory of [destination] must exist.
     */
    fun atomicWrite(destination: Path, fileSystem: FileSystem = defaultFileSystem, writer: (Sink) -> Unit) {
        val parent = destination.parent ?: error("Target must have a parent directory: $destination")
        ensureDirectoryExists(parent, fileSystem)

        val temporaryFilePath = Path(parent, destination.name + ".tmp")

        fileSystem.sink(temporaryFilePath).buffered().use { sink -> writer(sink) }

        fileSystem.atomicMove(temporaryFilePath, destination)
    }
}
