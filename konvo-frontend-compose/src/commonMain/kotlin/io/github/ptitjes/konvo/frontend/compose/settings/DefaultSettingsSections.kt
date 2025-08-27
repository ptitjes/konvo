package io.github.ptitjes.konvo.frontend.compose.settings

import io.github.ptitjes.konvo.frontend.compose.mcp.*
import io.github.ptitjes.konvo.frontend.compose.models.*
import io.github.ptitjes.konvo.frontend.compose.roleplay.*

val defaultSettingsSections = listOf(
    SettingsSection(
        titleKey = "appearance",
        panel = { AppearanceSettingsPanel() },
    ),
    SettingsSection(
        titleKey = "mcp",
        panel = { McpSettingsPanel() },
    ),
    SettingsSection(
        titleKey = "models",
        panel = { ModelProviderSettingsPanel() },
    ),
    SettingsSection(
        titleKey = "roleplay",
        panel = { RoleplaySettingsPanel() },
        children = listOf(
            SettingsSection(
                titleKey = "characters",
                scrollable = false,
                panel = { CharacterSettingsPanel() },
            ),
            SettingsSection(
                titleKey = "lorebooks",
                panel = { LorebooksSettingsPanel() },
                children = emptyList(),
            ),
            SettingsSection(
                titleKey = "personas",
                panel = { PersonaSettingsPanel() },
            ),
        ),
    ),
)
