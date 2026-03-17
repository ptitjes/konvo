package io.github.ptitjes.konvo.plugin.core.ui.compose.translations

import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.appearance.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.mcp.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.prompts.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.text.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.tools.*

internal data class Strings(
    val navigation: NavigationStrings,
    val agents: AgentStrings,
    val models: ModelStrings,
    val mcp: McpStrings,
    val prompts: PromptStrings,
    val tools: ToolStrings,
    val conversations: ConversationStrings,
    val appearance: AppearanceStrings,
    val settings: SettingsStrings,
    val formats: FormatStrings,
)
