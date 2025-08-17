package io.github.ptitjes.konvo.core.characters

/**
 * Character manager backed by a set of CharacterProviders.
 */
class DiCharacterCardManager(
    private val providers: Set<CharacterCardProvider>,
) : CharacterCardManager {

    private lateinit var _elements: List<CharacterCard>
    override val elements: List<CharacterCard>
        get() = _elements

    override suspend fun init() {
        _elements = providers.flatMap { it.query() }
    }
}
