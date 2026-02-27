package io.github.ptitjes.konvo.core.agents

import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.*

interface Agent {
    // TODO this should return a AgentSession
    suspend fun restoreSession(transcript: List<Action<*>>, conversation: InteractionDevice.Agent)
}
