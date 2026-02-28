package io.github.ptitjes.konvo.core.agents

import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.*

interface Agent {
    suspend fun restoreSession(
        transcript: ConversationTranscript,
        device: InteractionDevice.Agent,
    ): AgentSession
}

interface AgentSession {
    val isPaused: Boolean
    suspend fun pause()
    suspend fun resume()
    suspend fun close()
}
