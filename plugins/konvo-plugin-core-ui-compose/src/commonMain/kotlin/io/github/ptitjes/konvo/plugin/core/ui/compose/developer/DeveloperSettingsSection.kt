package io.github.ptitjes.konvo.plugin.core.ui.compose.developer

import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.strings
import io.github.ptitjes.syrup.specification.*

fun PluginSpecificationBuilder.developerSettingsSection() {
    SettingsSections {
        contribution {
            SettingsSection(
                titleKey = "developer",
                icon = Res.drawable.ic_mobile_code,
                title = { strings.settings.sectionTitles.getValue("developer") },
                panel = { DeveloperSettingsPanel() },
            )
        }
    }
}
