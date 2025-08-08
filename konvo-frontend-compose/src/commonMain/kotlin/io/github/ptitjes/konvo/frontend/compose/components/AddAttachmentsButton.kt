package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.material.icons.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.frontend.compose.attachments.*
import io.github.vinceglb.filekit.compose.*
import io.github.vinceglb.filekit.core.*

@Composable
fun AddAttachmentsButton(
    onAddAttachments: (List<Attachment>) -> Unit,
) {
    val launcher = rememberFilePickerLauncher(
        mode = PickerMode.Multiple(),
        type = PickerType.Image,
    ) { files -> files?.toList()?.map { it.createImageAttachement() }?.let { onAddAttachments(it) } }

    IconButton(onClick = { launcher.launch() }) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Add an attachment",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
