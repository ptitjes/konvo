package io.github.ptitjes.konvo.core.ai.characters

import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.base.*
import kotlinx.io.files.*

class FileSystemCharacterProvider(
    storagePaths: StoragePaths,
) : Provider<CharacterCard> {
    override val name: String? = null

    private val characterDirectoryPath = Path(storagePaths.dataDirectory, "characters")

    override suspend fun query(): List<CharacterCard> {
        return CharacterCard.loadCharacters(characterDirectoryPath)
    }
}
