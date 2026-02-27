package io.github.ptitjes.konvo.core.agents.toolkit

import ai.koog.agents.core.feature.config.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.tools.*

class ConversationFeatureConfig : FeatureConfig() {
    var conversationViewProvider: () -> InteractionDevice.Agent = { error("Not initialized") }
    var tools: List<ToolCard> = emptyList()
}
