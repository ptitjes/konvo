package io.github.ptitjes.konvo.plugin.core.ui.compose.mcp

import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.strings
import io.github.ptitjes.syrup.specification.*

fun PluginSpecificationBuilder.mcpSettingsSection() {
    SettingsSections {
        contribution {
            SettingsSection(
                titleKey = "mcp",
                icon = Res.drawable.ic_extension,
                title = { strings.settings.sectionTitles.getValue("mcp") },
                panel = { McpSettingsPanel() },
            )
        }
    }
}
