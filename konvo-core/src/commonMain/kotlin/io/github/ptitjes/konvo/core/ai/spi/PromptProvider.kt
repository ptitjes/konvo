package io.github.ptitjes.konvo.core.ai.spi

interface PromptProvider {
    suspend fun queryPrompts(): List<PromptCard>
}
