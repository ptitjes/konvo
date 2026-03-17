package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import androidx.compose.material3.*
import androidx.compose.runtime.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.events.Messaging.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.vinceglb.filekit.dialogs.*
import io.github.vinceglb.filekit.dialogs.compose.*
import org.jetbrains.compose.resources.*

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
            painter = painterResource(Res.drawable.ic_attach_file),
            contentDescription = i18n.conversations.addAttachmentAria,
        )
    }
}
