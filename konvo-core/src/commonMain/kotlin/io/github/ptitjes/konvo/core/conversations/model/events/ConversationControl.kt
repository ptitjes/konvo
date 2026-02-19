package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.serialization.*

@Serializable
sealed interface ConversationControl : Event.Payload {
    @Serializable
    @SerialName("conversation-control-title-change")
    data class TitleChange(
        val title: String,
    ) : ConversationControl, Event.Agent, Event.User

    @Serializable
    @SerialName("conversation-control-invite-agent")
    data class InviteAgent(
        val agentId: String,
        // TODO make this an AgentId
        val agentConfiguration: AgentConfiguration,
    ) : ConversationControl, Event.Agent, Event.User
}
