package io.github.ptitjes.konvo.core.conversations

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.coroutines.flow.*

/**
 * Provides a view of the conversation for UI components.
 * This interface allows UI components to send user actions into a conversation.
 *
 * @deprecated Use InteractionDevice.User instead
 */
@Deprecated(
    "Use InteractionDevice.User instead",
    ReplaceWith("InteractionDevice.User", "io.github.ptitjes.konvo.core.conversations.InteractionDevice")
)
interface ConversationUserView : InteractionDevice.User {

    val state: StateFlow<ConversationState>

    @Deprecated("Use actions instead", ReplaceWith("actions"))
    val events: SharedFlow<Action<*>>
        get() = actions

    @Deprecated("Use act instead", ReplaceWith("act(payload)"))
    suspend fun send(payload: Action.User) = act(payload)
}
