package io.github.ptitjes.konvo.frontend.compose.toolkit.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import io.github.ptitjes.konvo.frontend.compose.settings.*
import io.github.ptitjes.konvo.frontend.compose.settings.BaseColorScheme.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.settings.*

@Composable
fun KonvoTheme(
    colorSchemeOverride: BaseColorScheme? = null,
    content: @Composable () -> Unit,
) {
    val colorSchemeSetting by rememberSetting(AppearanceSettingsKey, null) { it.baseColorScheme }
    val colorScheme = colorSchemeOverride ?: colorSchemeSetting ?: System
    val isDarkTheme = when (colorScheme) {
        Dark -> true
        Light -> false
        System -> isInDarkTheme()
    }

    CompositionLocalProvider(
        LocalTheme provides Theme(isDarkTheme),
    ) {
        MaterialTheme(
            colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme(),
        ) {
            content()
        }
    }
}
