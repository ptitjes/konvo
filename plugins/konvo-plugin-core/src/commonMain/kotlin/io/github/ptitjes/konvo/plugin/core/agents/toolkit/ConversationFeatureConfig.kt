package io.github.ptitjes.konvo.plugin.core.agents.toolkit

import ai.koog.agents.core.feature.config.*
import io.github.ptitjes.konvo.plugin.core.conversations.*
import io.github.ptitjes.konvo.plugin.core.tools.*

class ConversationFeatureConfig : FeatureConfig() {
    var conversationViewProvider: () -> InteractionDevice.Agent = { error("Not initialized") }
    var tools: List<ToolCard> = emptyList()
}
