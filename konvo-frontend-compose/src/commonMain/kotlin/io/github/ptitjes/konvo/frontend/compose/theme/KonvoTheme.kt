package io.github.ptitjes.konvo.frontend.compose.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun KonvoTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val isDarkTheme = darkTheme ?: isInDarkTheme()

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
