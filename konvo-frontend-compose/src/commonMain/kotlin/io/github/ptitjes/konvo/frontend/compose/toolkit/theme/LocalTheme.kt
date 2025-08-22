package io.github.ptitjes.konvo.frontend.compose.toolkit.theme

import androidx.compose.runtime.*

val LocalTheme = compositionLocalOf { Theme() }

class Theme(val isDark: Boolean = false)
