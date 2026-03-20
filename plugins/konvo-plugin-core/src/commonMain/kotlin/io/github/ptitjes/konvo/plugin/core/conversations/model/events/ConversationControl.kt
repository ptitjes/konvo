package io.github.ptitjes.konvo.plugin.core.conversations.model.events

import io.github.ptitjes.konvo.plugin.core.agents.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import kotlinx.serialization.*

@Serializable
sealed interface ConversationControl : Action.Payload {
    @Serializable
    @SerialName("ConversationControl#TitleChange")
    data class TitleChange(
        val title: String,
    ) : ConversationControl, Action.Agent, Action.User

    @Serializable
    @SerialName("ConversationControl#InviteAgent")
    data class InviteAgent(
        val participantId: String,
        // TODO replace this with an AgentId, when configuration protocols are up
        val agentConfiguration: AgentConfiguration,
    ) : ConversationControl, Action.Agent, Action.User
}
