package io.github.ptitjes.konvo.core.prompts

import ai.koog.prompt.dsl.*

interface PromptCard {
    val name: String
    val description: String?

    suspend fun toPrompt(): Prompt
}
