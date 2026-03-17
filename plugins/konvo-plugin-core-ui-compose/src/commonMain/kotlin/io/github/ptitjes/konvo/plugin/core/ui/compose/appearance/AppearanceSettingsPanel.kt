package io.github.ptitjes.konvo.plugin.core.ui.compose.appearance

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*

@Composable
fun SettingsPanelScope.AppearanceSettingsPanel() {
    var settings by rememberMutableSettings(AppearanceSettingsKey)

    SettingsBox(
        title = i18n.appearance.baseColorSchemeTitle,
        description = i18n.appearance.baseColorSchemeDescription,
        bottomContent = {
            val optDark = i18n.appearance.baseColorSchemeOptionDark
            val optLight = i18n.appearance.baseColorSchemeOptionLight
            val optSystem = i18n.appearance.baseColorSchemeOptionSystem

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
