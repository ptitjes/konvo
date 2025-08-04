package io.github.ptitjes.konvo.frontend.compose.attachments

import ai.koog.prompt.message.*
import io.github.vinceglb.filekit.core.*

expect fun PlatformFile.createFileAttachement(): Attachment
