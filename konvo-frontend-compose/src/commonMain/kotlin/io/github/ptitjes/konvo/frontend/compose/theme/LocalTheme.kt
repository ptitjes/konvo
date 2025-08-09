package io.github.ptitjes.konvo.frontend.compose.theme

import androidx.compose.runtime.*

val LocalTheme = compositionLocalOf { Theme() }

class Theme(val isDark: Boolean = false)
