package io.github.ptitjes.konvo.core.spi

interface ToolProvider {
    suspend fun queryTools(): List<Tool>
}
