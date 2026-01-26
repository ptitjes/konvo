package io.github.ptitjes.konvo.frontend.compose.toolkit.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.*
import io.github.ptitjes.konvo.frontend.compose.resources.*
import io.github.ptitjes.konvo.frontend.compose.settings.*
import io.github.ptitjes.konvo.frontend.compose.settings.BaseColorScheme.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.settings.*
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
            colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme(),
            typography = MaterialTheme.typography.withFontFamily(
                FontFamily(Font(Res.font.InterVariable))
            ),
        ) {
            content()
        }
    }
}

fun Typography.withFontFamily(fontFamily: FontFamily) = Typography(
    displayLarge = displayLarge.copy(fontFamily = fontFamily),
    displayMedium = displayMedium.copy(fontFamily = fontFamily),
    displaySmall = displaySmall.copy(fontFamily = fontFamily),

    headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
    headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
    headlineSmall = headlineSmall.copy(fontFamily = fontFamily),

    titleLarge = titleLarge.copy(fontFamily = fontFamily),
    titleMedium = titleMedium.copy(fontFamily = fontFamily),
    titleSmall = titleSmall.copy(fontFamily = fontFamily),

    bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
    bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
    bodySmall = bodySmall.copy(fontFamily = fontFamily),

    labelLarge = labelLarge.copy(fontFamily = fontFamily),
    labelMedium = labelMedium.copy(fontFamily = fontFamily),
    labelSmall = labelSmall.copy(fontFamily = fontFamily)
)