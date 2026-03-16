package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.BaseColorScheme.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*
import org.jetbrains.compose.resources.*

@Composable
fun KonvoTheme(
    colorSchemeOverride: BaseColorScheme? = null,
    content: @Composable () -> Unit,
) {
    val colorSchemeSetting by rememberSetting(AppearanceSettingsKey) { it.baseColorScheme }
    val colorScheme = colorSchemeOverride ?: colorSchemeSetting
    val isDarkTheme = when (colorScheme) {
        Dark -> true
        Light -> false
        System -> isInDarkTheme()
    }

    CompositionLocalProvider(
        LocalTheme provides Theme(isDarkTheme),
    ) {
        MaterialTheme(
            colorScheme = if (isDarkTheme) darkScheme else lightScheme,
            typography = MaterialTheme.typography.withFontFamilies(
                displayFontFamily = FontFamily(Font(Res.font.InterVariable)),
                bodyFontFamily = FontFamily(Font(Res.font.InterVariable)),
            ),
        ) {
            content()
        }
    }
}

fun Typography.withFontFamilies(
    displayFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
) = Typography(
    displayLarge = displayLarge.copy(fontFamily = displayFontFamily),
    displayMedium = displayMedium.copy(fontFamily = displayFontFamily),
    displaySmall = displaySmall.copy(fontFamily = displayFontFamily),

    headlineLarge = headlineLarge.copy(fontFamily = displayFontFamily),
    headlineMedium = headlineMedium.copy(fontFamily = displayFontFamily),
    headlineSmall = headlineSmall.copy(fontFamily = displayFontFamily),

    titleLarge = titleLarge.copy(fontFamily = displayFontFamily),
    titleMedium = titleMedium.copy(fontFamily = displayFontFamily),
    titleSmall = titleSmall.copy(fontFamily = displayFontFamily),

    bodyLarge = bodyLarge.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Thin),
    bodyMedium = bodyMedium.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Thin),
    bodySmall = bodySmall.copy(fontFamily = bodyFontFamily, fontWeight = FontWeight.Thin),

    labelLarge = labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = labelMedium.copy(fontFamily = bodyFontFamily),
    labelSmall = labelSmall.copy(fontFamily = bodyFontFamily)
)
