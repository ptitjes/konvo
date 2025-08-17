package io.github.ptitjes.konvo.core.ai.spi

import ai.koog.prompt.dsl.*

interface PromptCard : NamedCard {
    override val name: String
    val description: String?

    suspend fun toPrompt(): Prompt
}
