package io.github.ptitjes.konvo.core.conversation

import kotlinx.coroutines.flow.*

/**
 * Provides a view of the conversation for UI components.
 * This interface allows UI components to send user events and receive assistant events.
 */
interface ConversationUiView {
    /**
     * Flow of assistant events from the conversation.
     */
    val assistantEvents: SharedFlow<AssistantEvent>

    /**
     * Sends a user event to the conversation.
     *
     * @param event The user event to send
     */
    suspend fun sendUserEvent(event: UserEvent)
}
