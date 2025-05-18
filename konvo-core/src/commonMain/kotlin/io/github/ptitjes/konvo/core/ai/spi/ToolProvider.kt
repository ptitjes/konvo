package io.github.ptitjes.konvo.core.ai.spi

interface ToolProvider {
    suspend fun queryTools(): List<Tool>
}
