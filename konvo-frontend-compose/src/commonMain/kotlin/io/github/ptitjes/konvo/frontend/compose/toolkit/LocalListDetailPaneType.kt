package io.github.ptitjes.konvo.frontend.compose.toolkit

import androidx.compose.runtime.*

val LocalListDetailPaneType = compositionLocalOf<ListDetailPaneType> { error("No ListDetailPaneType provided") }
