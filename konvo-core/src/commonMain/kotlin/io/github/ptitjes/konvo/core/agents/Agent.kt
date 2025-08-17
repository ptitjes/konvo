package io.github.ptitjes.konvo.core.agents

import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.core.conversation.model.*

interface Agent {
    suspend fun restorePrompt(events: List<Event>)
    suspend fun joinConversation(conversation: ConversationAgentView)
}
