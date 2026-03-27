package io.github.ptitjes.konvo.plugin.core.ui.compose.models

import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.syrup.specification.*
import org.kodein.di.*

fun PluginSpecificationBuilder.modelsSettingsSection() {
    SettingsSections {
        contribution {
            SettingsSection(
                titleKey = "models",
                icon = Res.drawable.ic_memory,
                title = { i18n.models.settingsTitle },
                presenterFactory = { new(::ModelProviderSettingsPresenter) },
                panel = { state -> ModelProviderSettingsPanel(state) },
            )
        }
    }

    modelStrings()
}
