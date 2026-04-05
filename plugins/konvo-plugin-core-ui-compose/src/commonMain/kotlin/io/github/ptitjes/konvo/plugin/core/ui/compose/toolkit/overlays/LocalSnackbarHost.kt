package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.overlays

import androidx.compose.material3.*
import androidx.compose.runtime.*

val LocalSnackbarHost = compositionLocalOf<SnackbarHostState> { error("No SnackbarHostState provided") }
