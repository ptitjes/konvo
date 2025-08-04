package io.github.ptitjes.konvo.core.conversation

import kotlinx.coroutines.flow.*

/**
 * Provides a view of the conversation for AI agents.
 * This interface allows agents to receive user events and send assistant events.
 */
interface ConversationAgentView {
    /**
     * Flow of user events from the conversation.
     */
    val userEvents: SharedFlow<UserEvent>

    /**
     * Sends an assistant event to the conversation.
     *
     * @param event The assistant event to send
     */
    suspend fun sendAssistantEvent(event: AssistantEvent)
}
