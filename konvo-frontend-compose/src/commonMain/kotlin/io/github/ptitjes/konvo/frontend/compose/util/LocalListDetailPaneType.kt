package io.github.ptitjes.konvo.frontend.compose.util

import androidx.compose.runtime.*
import io.github.ptitjes.konvo.frontend.compose.*

val LocalListDetailPaneType = compositionLocalOf<ListDetailPaneType> { error("No ListDetailPaneType provided") }
