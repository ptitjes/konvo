package io.github.ptitjes.konvo.core.characters.providers

import io.github.ptitjes.konvo.core.characters.CharacterCard

internal data class DefaultCharacterCard(
    override val id: String,
    override val name: String,
    override val avatarUrl: String? = null,
    override val description: String,
    override val personality: String,
    override val scenario: String,
    override val messageExample: String,
    override val greetings: List<String>,
    override val tags: List<String>,
) : CharacterCard
