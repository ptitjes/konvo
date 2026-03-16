package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.spi

import androidx.compose.runtime.*

val LocalViewRegistry =
    compositionLocalOf<ConversationViewRegistry> { error("No LocalViewRegistry provided") }
