package io.github.ptitjes.konvo.frontend.compose.settings

import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*

val defaultSettingsSections = listOf(
    SettingsSection.WithKey(
        title = "Appearance",
        key = AppearanceSettingsKey,
        panel = ::AppearanceSettingsPanel,
    ),
    SettingsSection.WithKey(
        title = "MCP servers",
        key = McpSettingsKey,
        panel = ::McpSettingsPanel,
    ),
    SettingsSection.WithKey(
        title = "Model providers",
        key = ModelProviderSettingsKey,
        panel = ::ModelProviderSettingsPanel,
    ),
    SettingsSection.WithKey(
        title = "Roleplay",
        key = RoleplaySettingsKey,
        panel = ::RoleplaySettingsPanel,
        children = listOf(
            SettingsSection.WithKey(
                title = "Characters",
                key = CharacterSettingsKey,
                panel = ::CharacterSettingsPanel,
            ),
            SettingsSection.WithoutKey(
                title = "Lorebooks",
                panel = ::LorebooksSettingsPanel,
                children = emptyList(),
            ),
            SettingsSection.WithKey(
                title = "Personas",
                key = PersonaSettingsKey,
                panel = ::PersonaSettingsPanel,
            ),
        ),
    ),
)
