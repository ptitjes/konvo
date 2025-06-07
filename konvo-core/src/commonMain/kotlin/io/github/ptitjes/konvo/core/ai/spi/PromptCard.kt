package io.github.ptitjes.konvo.core.ai.spi

import ai.koog.prompt.dsl.Prompt

interface PromptCard {
    val name: String
    val description: String?

    suspend fun toPrompt(): Prompt
}
