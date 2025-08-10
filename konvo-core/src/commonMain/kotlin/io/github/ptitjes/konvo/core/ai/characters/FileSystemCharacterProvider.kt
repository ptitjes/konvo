package io.github.ptitjes.konvo.core.ai.characters

import io.github.ptitjes.konvo.core.ai.spi.*
import kotlinx.io.files.*

class FileSystemCharacterProvider(
    private val dataDirectory: String,
) : CharacterProvider {

    private val characterDirectoryPath = Path(dataDirectory, "characters")

    override fun queryCharacters(): List<CharacterCard> {
        return CharacterCard.loadCharacters(characterDirectoryPath)
    }
}
