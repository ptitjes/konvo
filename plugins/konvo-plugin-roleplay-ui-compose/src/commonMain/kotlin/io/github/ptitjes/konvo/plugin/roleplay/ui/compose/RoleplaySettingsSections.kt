package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import io.github.ptitjes.konvo.plugin.core.ui.compose.roleplay.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.roleplay.ui.compose.resources.*
import io.github.ptitjes.syrup.specification.*

internal fun PluginSpecificationBuilder.roleplaySettingsSections() {
    SettingsSections {
        contribution {
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
            )
        }
    }
}
