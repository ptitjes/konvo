package io.github.ptitjes.konvo.plugin.core.ui.compose.models

import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import io.github.ptitjes.syrup.specification.*

fun PluginSpecificationBuilder.modelsSettingsSection() {
    SettingsSections {
        contribution {
            SettingsSection(
                titleKey = "models",
                icon = Res.drawable.ic_memory,
                title = { strings.models.settingsTitle },
                panel = { ModelProviderSettingsPanel() },
            )
        }
    }

    modelStrings()
}

private fun PluginSpecificationBuilder.modelStrings() {
    i18nStrings<ModelStrings>("ar-SA") { ArStrings.models }
    i18nStrings<ModelStrings>("en-US") { EnStrings.models }
    i18nStrings<ModelStrings>("es-ES") { EsStrings.models }
    i18nStrings<ModelStrings>("fr-FR") { FrStrings.models }
    i18nStrings<ModelStrings>("hi-IN") { HiStrings.models }
    i18nStrings<ModelStrings>("zh-CN") { ZhStrings.models }
}
