package io.github.ptitjes.konvo.frontend.compose.translations

import io.github.ptitjes.konvo.frontend.compose.agents.*
import io.github.ptitjes.konvo.frontend.compose.mcp.*
import io.github.ptitjes.konvo.frontend.compose.models.*
import io.github.ptitjes.konvo.frontend.compose.prompts.*
import io.github.ptitjes.konvo.frontend.compose.roleplay.*
import io.github.ptitjes.konvo.frontend.compose.tools.*

internal data class Strings(
    val agents: AgentStrings,
    val models: ModelStrings,
    val mcp: McpStrings,
    val prompts: PromptStrings,
    val tools: ToolStrings,
    val roleplay: RoleplayStrings,
)
