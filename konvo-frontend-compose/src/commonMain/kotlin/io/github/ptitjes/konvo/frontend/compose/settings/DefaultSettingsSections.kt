package io.github.ptitjes.konvo.frontend.compose.settings

import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*

val defaultSettingsSections = listOf(
    SettingsSection(
        key = AppearanceSettingsKey,
        title = "Appearance",
        panel = ::AppearanceSettingsPanel,
    ),
    SettingsSection(
        key = CharacterSettingsKey,
        title = "Characters",
        panel = ::CharacterSettingsPanel,
    ),
    SettingsSection(
        key = McpSettingsKey,
        title = "MCP servers",
        panel = ::McpSettingsPanel,
    ),
    SettingsSection(
        key = ModelProviderSettingsKey,
        title = "Model providers",
        panel = ::ModelProviderSettingsPanel,
    ),
    SettingsSection(
        key = RoleplayAgentSettingsKey,
        title = "Roleplay Agent",
        panel = ::RoleplayAgentSettingsPanel,
    ),
)
