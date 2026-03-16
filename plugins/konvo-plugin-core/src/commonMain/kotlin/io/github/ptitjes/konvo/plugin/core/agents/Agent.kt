package io.github.ptitjes.konvo.plugin.core.agents

import io.github.ptitjes.konvo.plugin.core.conversations.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.events.*

interface Agent {
    suspend fun restoreSession(
        transcript: ConversationTranscript,
        invite: Action<ConversationControl.InviteAgent>,
        device: InteractionDevice.Agent,
    ): AgentSession
}
