package io.github.ptitjes.konvo.frontend.compose.roleplay

import io.github.ptitjes.konvo.core.roleplay.providers.*
import io.github.vinceglb.filekit.core.*
import kotlinx.io.files.*

actual suspend fun PlatformFile.importCharacter(provider: FileSystemCharacterProvider) {
    provider.add(Path(file.absolutePath))
}
