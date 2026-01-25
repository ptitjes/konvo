package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.material.icons.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import io.github.vinceglb.filekit.dialogs.*
import io.github.vinceglb.filekit.dialogs.compose.*

@Composable
fun AddAttachmentsButton(
    onAddAttachments: (List<Attachment>) -> Unit,
) {
    val launcher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        mode = FileKitMode.Multiple(),
    ) { files -> files?.toList()?.map { it.createImageAttachment() }?.let { onAddAttachments(it) } }

    IconButton(onClick = { launcher.launch() }) {
        Icon(
            imageVector = Icons.Rounded.AttachFile,
            contentDescription = strings.conversations.addAttachmentAria,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
