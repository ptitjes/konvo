package io.github.ptitjes.konvo.plugin.core.ui.compose.mcp

import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import io.github.ptitjes.syrup.specification.*
import org.kodein.di.*

fun PluginSpecificationBuilder.mcpSettingsSection() {
    SettingsSections {
        contribution {
            SettingsSection(
                titleKey = "mcp",
                icon = Res.drawable.ic_extension,
                title = { i18n.mcp.settingsTitle },
                presenterFactory = { new(::McpSettingsPresenter) },
                panel = { state -> McpSettingsPanel(state) },
            )
        }
    }

    mcpStrings()
}

private fun PluginSpecificationBuilder.mcpStrings() {
    i18nStrings<McpStrings>("ar-SA") { ArStrings.mcp }
    i18nStrings<McpStrings>("en-US") { EnStrings.mcp }
    i18nStrings<McpStrings>("es-ES") { EsStrings.mcp }
    i18nStrings<McpStrings>("fr-FR") { FrStrings.mcp }
    i18nStrings<McpStrings>("hi-IN") { HiStrings.mcp }
    i18nStrings<McpStrings>("zh-CN") { ZhStrings.mcp }
}
