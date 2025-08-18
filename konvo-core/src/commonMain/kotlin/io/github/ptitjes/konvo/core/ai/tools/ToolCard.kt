package io.github.ptitjes.konvo.core.ai.tools

import ai.koog.agents.core.tools.*

interface ToolCard {
    val name: String
    val description: String?
    val parameters: ToolParameters
    val requiresVetting: Boolean

    suspend fun toTool(): Tool<*, *>
}
