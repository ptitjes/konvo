package io.github.ptitjes.konvo.frontend.compose.attachments

import io.github.ptitjes.konvo.core.conversation.*
import io.github.vinceglb.filekit.core.*

expect fun PlatformFile.createImageAttachement(): Attachment
