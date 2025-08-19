package io.github.ptitjes.konvo.core.characters.providers

import io.github.ptitjes.konvo.core.characters.*

internal data class DefaultCharacterCard(
    override val id: String,
    override val name: String,
    override val avatarUrl: String?,
    override val description: String,
    override val personality: String,
    override val scenario: String,
    override val dialogueExamples: String,
    override val systemPrompt: String?,
    override val postHistoryInstructions: String?,
    override val greetings: List<String>,
    override val tags: List<String>,
) : CharacterCard
