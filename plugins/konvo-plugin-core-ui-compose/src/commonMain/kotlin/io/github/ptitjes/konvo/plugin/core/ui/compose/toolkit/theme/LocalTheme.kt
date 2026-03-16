package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.theme

import androidx.compose.runtime.*

val LocalTheme = compositionLocalOf { Theme() }

class Theme(val isDark: Boolean = false)
