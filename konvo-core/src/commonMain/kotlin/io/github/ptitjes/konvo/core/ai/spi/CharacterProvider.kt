package io.github.ptitjes.konvo.core.ai.spi

interface CharacterProvider {
    fun queryCharacters(): List<CharacterCard>
}
