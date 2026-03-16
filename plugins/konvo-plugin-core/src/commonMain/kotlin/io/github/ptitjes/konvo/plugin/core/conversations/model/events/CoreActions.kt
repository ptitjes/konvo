package io.github.ptitjes.konvo.plugin.core.conversations.model.events

import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*

@OptIn(ExperimentalSerializationApi::class)
val CoreActions = SerializersModule {
    polymorphic(Action.Payload::class) {
        subclassesOfSealed<ConversationControl>()
        subclassesOfSealed<Presence>()
        subclassesOfSealed<AgentCapabilities>()
        subclassesOfSealed<AgentProcessing>()
        subclassesOfSealed<ToolUsage>()
        subclassesOfSealed<Messaging>()
    }
}
