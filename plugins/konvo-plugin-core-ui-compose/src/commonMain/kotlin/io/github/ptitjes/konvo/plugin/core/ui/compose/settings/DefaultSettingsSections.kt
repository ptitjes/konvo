package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import io.github.ptitjes.konvo.plugin.core.ui.compose.mcp.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.roleplay.*

val defaultSettingsSections = listOf(
    SettingsSection(
        titleKey = "appearance",
        icon = Res.drawable.ic_palette,
        panel = { AppearanceSettingsPanel() },
    ),
    SettingsSection(
        titleKey = "mcp",
        icon = Res.drawable.ic_extension,
        panel = { McpSettingsPanel() },
    ),
    SettingsSection(
        titleKey = "models",
        icon = Res.drawable.ic_memory,
        panel = { ModelProviderSettingsPanel() },
    ),
    SettingsSection(
        titleKey = "developer",
        icon = Res.drawable.ic_mobile_code,
        panel = { DeveloperSettingsPanel() },
    ),
    SettingsSection(
        titleKey = "roleplay",
        icon = Res.drawable.ic_theater_comedy,
        panel = { RoleplaySettingsPanel() },
        children = listOf(
            SettingsSection(
                titleKey = "characters",
                icon = Res.drawable.ic_person,
                scrollable = false,
                panel = { CharacterSettingsPanel() },
            ),
            SettingsSection(
                titleKey = "lorebooks",
                icon = Res.drawable.ic_menu_book,
                panel = { LorebooksSettingsPanel() },
                children = emptyList(),
            ),
            SettingsSection(
                titleKey = "personas",
                icon = Res.drawable.ic_comedy_mask,
                panel = { PersonaSettingsPanel() },
            ),
        ),
    ),
)
