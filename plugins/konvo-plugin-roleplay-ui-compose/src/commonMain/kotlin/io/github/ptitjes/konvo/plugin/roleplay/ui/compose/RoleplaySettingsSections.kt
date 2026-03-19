package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.roleplay.ui.compose.resources.*
import io.github.ptitjes.syrup.specification.*

internal fun PluginSpecificationBuilder.roleplaySettingsSections() {
    SettingsSections {
        contribution {
            SettingsSection(
                titleKey = "roleplay",
                icon = Res.drawable.ic_theater_comedy,
                title = { i18n.roleplay.roleplaySettingsTitle },
                panel = { RoleplaySettingsPanel() },
                children = listOf(
                    SettingsSection(
                        titleKey = "characters",
                        icon = Res.drawable.ic_person,
                        title = { i18n.roleplay.charactersSettingsTitle },
                        panel = { CharacterSettingsPanel() },
                        scrollable = false,
                    ),
                    SettingsSection(
                        titleKey = "lorebooks",
                        icon = Res.drawable.ic_menu_book,
                        title = { i18n.roleplay.lorebooksSettingsTitle },
                        panel = { LorebooksSettingsPanel() },
                    ),
                    SettingsSection(
                        titleKey = "personas",
                        icon = Res.drawable.ic_comedy_mask,
                        title = { i18n.roleplay.personasSettingsTitle },
                        panel = { PersonaSettingsPanel() },
                    ),
                ),
            )
        }
    }
}
