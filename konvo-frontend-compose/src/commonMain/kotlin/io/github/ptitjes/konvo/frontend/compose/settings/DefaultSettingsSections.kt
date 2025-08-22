package io.github.ptitjes.konvo.frontend.compose.settings

import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.frontend.compose.mcp.*
import io.github.ptitjes.konvo.frontend.compose.models.*
import io.github.ptitjes.konvo.frontend.compose.roleplay.*

val defaultSettingsSections = listOf(
    SettingsSection.WithKey(
        titleKey = "appearance",
        key = AppearanceSettingsKey,
        panel = ::AppearanceSettingsPanel,
    ),
    SettingsSection.WithKey(
        titleKey = "mcp",
        key = McpSettingsKey,
        panel = ::McpSettingsPanel,
    ),
    SettingsSection.WithKey(
        titleKey = "models",
        key = ModelProviderSettingsKey,
        panel = ::ModelProviderSettingsPanel,
    ),
    SettingsSection.WithKey(
        titleKey = "roleplay",
        key = RoleplaySettingsKey,
        panel = ::RoleplaySettingsPanel,
        children = listOf(
            SettingsSection.WithKey(
                titleKey = "characters",
                scrollable = false,
                key = CharacterSettingsKey,
                panel = ::CharacterSettingsPanel,
            ),
            SettingsSection.WithoutKey(
                titleKey = "lorebooks",
                panel = ::LorebooksSettingsPanel,
                children = emptyList(),
            ),
            SettingsSection.WithKey(
                titleKey = "personas",
                key = PersonaSettingsKey,
                panel = ::PersonaSettingsPanel,
            ),
        ),
    ),
)
