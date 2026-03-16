package io.github.ptitjes.konvo.plugin.core.tools

import ai.koog.agents.core.tools.*

interface ToolCard {
    val name: String
    val description: String?
    val parameters: ToolParameters
    val requiresVetting: Boolean

    fun toTool(): Tool<*, *>
}
