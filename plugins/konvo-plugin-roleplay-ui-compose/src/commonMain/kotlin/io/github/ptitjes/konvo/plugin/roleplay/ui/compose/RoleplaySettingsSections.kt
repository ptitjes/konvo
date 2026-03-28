package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.roleplay.ui.compose.resources.*
import io.github.ptitjes.syrup.specification.*
import org.kodein.di.*

internal fun PluginSpecificationBuilder.roleplaySettingsSections() {
    SettingsSections {
        contribution {
            SettingsSection(
                titleKey = "roleplay",
                icon = Res.drawable.ic_theater_comedy,
                title = { i18n.roleplay.roleplaySettingsTitle },
                presenterFactory = { new(::RoleplaySettingsPresenter, it) },
                panel = { state -> RoleplaySettingsPanel(state) },
                children = listOf(
                    SettingsSection(
                        titleKey = "characters",
                        icon = Res.drawable.ic_person,
                        title = { i18n.roleplay.charactersSettingsTitle },
                        presenterFactory = { new(::CharacterSettingsPresenter) },
                        panel = { state -> CharacterSettingsPanel(state) },
                        scrollable = false,
                    ),
                    SettingsSection(
                        titleKey = "lorebooks",
                        icon = Res.drawable.ic_menu_book,
                        title = { i18n.roleplay.lorebooksSettingsTitle },
                        presenterFactory = { new(::LorebooksSettingsPresenter) },
                        panel = { state -> LorebooksSettingsPanel(state) },
                    ),
                    SettingsSection(
                        titleKey = "personas",
                        icon = Res.drawable.ic_comedy_mask,
                        title = { i18n.roleplay.personasSettingsTitle },
                        presenterFactory = { new(::PersonaSettingsPresenter) },
                        panel = { PersonaSettingsPanel(it) },
                    ),
                ),
            )
        }
    }
}
