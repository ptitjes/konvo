package io.github.ptitjes.konvo.core.conversations

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.coroutines.flow.*

/**
 * A device through which a participant can interact with a conversation.
 */
sealed interface InteractionDevice {
    val participant: Participant
    val actions: SharedFlow<Action<*>>

    /**
     * Device for agent participants.
     */
    interface Agent : InteractionDevice {
        override val participant: Participant.Agent
        suspend fun act(payload: Action.Agent, interaction: Interaction? = null)
        suspend fun startInteraction(
            protocol: InteractionProtocol,
            parent: Interaction? = null,
            trigger: Action<*>? = null
        ): Interaction
        suspend fun endInteraction(interaction: Interaction)
    }

    /**
     * Device for user participants.
     */
    interface User : InteractionDevice {
        override val participant: Participant.User
        suspend fun act(payload: Action.User, interaction: Interaction? = null)
    }
}
