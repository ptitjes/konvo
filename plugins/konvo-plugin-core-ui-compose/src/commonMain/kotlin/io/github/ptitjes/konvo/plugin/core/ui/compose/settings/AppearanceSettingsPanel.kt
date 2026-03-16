package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*

@Composable
fun SettingsPanelScope.AppearanceSettingsPanel() {
    var settings by rememberMutableSettings(AppearanceSettingsKey)

    SettingsBox(
        title = strings.settings.appearanceBaseColorSchemeTitle,
        description = strings.settings.appearanceBaseColorSchemeDescription,
        bottomContent = {
            val optDark = strings.settings.appearanceBaseColorSchemeOptionDark
            val optLight = strings.settings.appearanceBaseColorSchemeOptionLight
            val optSystem = strings.settings.appearanceBaseColorSchemeOptionSystem

            GenericSelector(
                modifier = Modifier.fillMaxWidth(),
                selectedItem = settings.baseColorScheme,
                onSelectItem = { settings = settings.copy(baseColorScheme = it) },
                options = BaseColorScheme.entries,
                itemLabeler = {
                    when (it) {
                        BaseColorScheme.Dark -> optDark
                        BaseColorScheme.Light -> optLight
                        BaseColorScheme.System -> optSystem
                    }
                },
            )
        }
    )
}
