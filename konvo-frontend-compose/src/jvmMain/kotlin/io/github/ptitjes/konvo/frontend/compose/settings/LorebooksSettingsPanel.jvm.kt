package io.github.ptitjes.konvo.frontend.compose.settings

import io.github.ptitjes.konvo.core.roleplay.providers.*
import io.github.vinceglb.filekit.core.*
import kotlinx.io.files.*

actual suspend fun PlatformFile.importLorebook(provider: FileSystemLorebookProvider) {
    // On JVM, PlatformFile exposes the underlying java.io.File
    provider.add(Path(file.absolutePath))
}
