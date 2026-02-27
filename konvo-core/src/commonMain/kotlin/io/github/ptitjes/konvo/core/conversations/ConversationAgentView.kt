package io.github.ptitjes.konvo.core.conversations

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.coroutines.flow.*

/**
 * Provides a view of the conversation for AI agents.
 * This interface allows agents to send assistant actions into a conversation.
 *
 * @deprecated Use InteractionDevice.Agent instead
 */
@Deprecated(
    "Use InteractionDevice.Agent instead",
    ReplaceWith("InteractionDevice.Agent", "io.github.ptitjes.konvo.core.conversations.InteractionDevice")
)
interface ConversationAgentView : InteractionDevice.Agent {

    @Deprecated("Use actions instead", ReplaceWith("actions"))
    val events: SharedFlow<Action<*>>
        get() = actions

    @Deprecated("Use act instead", ReplaceWith("act(payload)"))
    suspend fun send(payload: Action.Agent) = act(payload)
}
