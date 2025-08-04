package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import io.github.vinceglb.filekit.core.*

@Composable
fun SelectedAttachementsView(attachments: List<PlatformFile>) {
    Row(
        modifier = Modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        attachments.forEach { attachment ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.heightIn(max = 100.dp),
                elevation = 2.dp,
            ) {
                AttachmentView(attachment)
            }
        }
    }
}
