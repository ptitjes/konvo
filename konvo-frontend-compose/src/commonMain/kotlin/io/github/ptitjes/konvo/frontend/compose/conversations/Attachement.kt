package io.github.ptitjes.konvo.frontend.compose.conversations

import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.*
import io.github.vinceglb.filekit.*

expect fun PlatformFile.createImageAttachment(): Attachment
