package io.github.ptitjes.konvo.plugin.core.conversations

import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.syrup.*

interface InteractionProtocolRegistry {
    val protocolsById: Map<String, InteractionProtocol>
    val idByProtocol: Map<InteractionProtocol, String>
}

internal class DefaultInteractionProtocolRegistry(pluginContext: PluginContext) : InteractionProtocolRegistry {
    private val protocols by pluginContext.sourcedContributions(InteractionProtocols)

    override val protocolsById by lazy {
        protocols.associate { (source, protocol) -> "${source.id.value}/${protocol.id}" to protocol }
    }

    override val idByProtocol by lazy {
        protocols.associate { (source, protocol) -> protocol to "${source.id.value}/${protocol.id}" }
    }
}
