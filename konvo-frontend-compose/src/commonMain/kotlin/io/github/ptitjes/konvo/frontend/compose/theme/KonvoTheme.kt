package io.github.ptitjes.konvo.frontend.compose.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import io.github.ptitjes.konvo.core.settings.*
import io.github.ptitjes.konvo.frontend.compose.util.*

@Composable
fun KonvoTheme(
    colorSchemeOverride: BaseColorScheme? = null,
    content: @Composable () -> Unit,
) {
    val colorSchemeSetting by rememberSetting(AppearanceSettingsKey, null) { it.baseColorScheme }
    val colorScheme = colorSchemeOverride ?: colorSchemeSetting ?: BaseColorScheme.System
    val isDarkTheme = when (colorScheme) {
        BaseColorScheme.Dark -> true
        BaseColorScheme.Light -> false
        BaseColorScheme.System -> isInDarkTheme()
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
