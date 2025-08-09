package io.github.ptitjes.konvo.frontend.compose.theme

import androidx.compose.runtime.*

@Composable
fun isInDarkTheme() = _isInDarkTheme()

@Composable
internal expect fun _isInDarkTheme(): Boolean
