package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.theme

import androidx.compose.runtime.*

@Composable
fun isInDarkTheme() = _isInDarkTheme()

@Composable
internal expect fun _isInDarkTheme(): Boolean
