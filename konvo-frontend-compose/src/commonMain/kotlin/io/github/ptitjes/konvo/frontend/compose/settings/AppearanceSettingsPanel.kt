package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*

@Composable
fun AppearanceSettingsPanel(
    settings: AppearanceSettings,
    updateSettings: ((AppearanceSettings) -> AppearanceSettings) -> Unit,
) {
    SettingsBox(
        title = "Base color scheme",
        description = "The color scheme used for the application.",
        bottomContent = {
            GenericSelector(
                modifier = Modifier.fillMaxWidth(),
                selectedItem = settings.baseColorScheme,
                onSelectItem = { updateSettings { previous -> previous.copy(baseColorScheme = it) } },
                options = BaseColorScheme.entries,
                itemLabeler = {
                    when (it) {
                        BaseColorScheme.Dark -> "Dark"
                        BaseColorScheme.Light -> "Light"
                        BaseColorScheme.System -> "Adapt to system"
                    }
                },
            )
        }
    )
}
