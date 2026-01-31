package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import io.github.ptitjes.konvo.frontend.compose.mcp.*
import io.github.ptitjes.konvo.frontend.compose.models.*
import io.github.ptitjes.konvo.frontend.compose.roleplay.*

val defaultSettingsSections = listOf(
    SettingsSection(
        titleKey = "appearance",
        icon = Icons.Default.Palette,
        panel = { AppearanceSettingsPanel() },
    ),
    SettingsSection(
        titleKey = "mcp",
        icon = Icons.Default.Extension,
        panel = { McpSettingsPanel() },
    ),
    SettingsSection(
        titleKey = "models",
        icon = Icons.Default.Memory,
        panel = { ModelProviderSettingsPanel() },
    ),
    SettingsSection(
        titleKey = "developer",
        icon = Icons.Default.DeveloperMode,
        panel = { DeveloperSettingsPanel() },
    ),
    SettingsSection(
        titleKey = "roleplay",
        icon = Icons.Default.TheaterComedy,
        panel = { RoleplaySettingsPanel() },
        children = listOf(
            SettingsSection(
                titleKey = "characters",
                icon = Icons.Default.Person,
                scrollable = false,
                panel = { CharacterSettingsPanel() },
            ),
            SettingsSection(
                titleKey = "lorebooks",
                icon = Icons.Default.MenuBook,
                panel = { LorebooksSettingsPanel() },
                children = emptyList(),
            ),
            SettingsSection(
                titleKey = "personas",
                icon = Icons.Default.Face,
                panel = { PersonaSettingsPanel() },
            ),
        ),
    ),
)
