package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import io.github.ptitjes.konvo.plugin.core.conversations.model.events.Messaging.*
import io.github.vinceglb.filekit.*

expect fun PlatformFile.createImageAttachment(): Attachment
