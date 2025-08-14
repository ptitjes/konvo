package io.github.ptitjes.konvo.core.ai.characters

import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.base.*
import kotlinx.io.files.*

class FileSystemCharacterProvider(
    storagePaths: StoragePaths,
) : CharacterProvider {

    private val characterDirectoryPath = Path(storagePaths.dataDirectory, "characters")

    override fun queryCharacters(): List<CharacterCard> {
        return CharacterCard.loadCharacters(characterDirectoryPath)
    }
}
