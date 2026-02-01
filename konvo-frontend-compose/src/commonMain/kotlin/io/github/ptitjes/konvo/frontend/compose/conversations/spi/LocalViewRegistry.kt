package io.github.ptitjes.konvo.frontend.compose.conversations.spi

import androidx.compose.runtime.*

val LocalViewRegistry =
    compositionLocalOf<ConversationViewRegistry> { error("No LocalViewRegistry provided") }
