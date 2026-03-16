package io.github.ptitjes.konvo.plugin.core.ui.compose.roleplay

import io.github.ptitjes.konvo.plugin.core.roleplay.providers.*
import io.github.vinceglb.filekit.*
import kotlinx.io.files.*

actual suspend fun PlatformFile.importCharacter(provider: FileSystemCharacterProvider) {
    provider.add(Path(file.absolutePath))
}
