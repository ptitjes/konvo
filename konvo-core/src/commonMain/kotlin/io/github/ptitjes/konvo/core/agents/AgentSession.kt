package io.github.ptitjes.konvo.core.agents

interface AgentSession {
    val isPaused: Boolean
    suspend fun pause()
    suspend fun resume()
    suspend fun close()
}
