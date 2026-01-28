package io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive

import androidx.compose.runtime.*

val LocalListDetailPaneType = compositionLocalOf<ListDetailPaneType> { error("No ListDetailPaneType provided") }
