package io.github.ptitjes.konvo.plugin.core.ui.compose.models

import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.strings
import io.github.ptitjes.syrup.specification.*

fun PluginSpecificationBuilder.modelsSettingsSection() {
    SettingsSections {
        contribution {
            SettingsSection(
                titleKey = "models",
                icon = Res.drawable.ic_memory,
                title = { strings.settings.sectionTitles.getValue("models") },
                panel = { ModelProviderSettingsPanel() },
            )
        }
    }
}
