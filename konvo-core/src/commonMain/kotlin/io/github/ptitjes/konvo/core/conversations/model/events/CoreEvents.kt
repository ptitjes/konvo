package io.github.ptitjes.konvo.core.conversations.model.events

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*

@OptIn(ExperimentalSerializationApi::class)
val CoreEvents = SerializersModule {
    polymorphic(Event.Payload::class) {
        subclassesOfSealed<Presence>()
        subclassesOfSealed<AgentCapabilities>()
        subclassesOfSealed<AgentProcessing>()
        subclassesOfSealed<ToolUsage>()
        subclassesOfSealed<Messaging>()
    }
}
