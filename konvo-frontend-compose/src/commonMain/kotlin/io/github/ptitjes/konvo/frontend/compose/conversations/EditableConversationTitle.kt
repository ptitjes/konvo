package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.input.*
import io.github.ptitjes.konvo.core.conversations.model.*

@Composable
fun EditableConversationTitle(
    conversation: ConversationDigest,
    onTitleChange: (String) -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }

    var titleField by remember(conversation.id) {
        mutableStateOf(TextFieldValue(conversation.title))
    }

    // Keep local text in sync with repository updates when not focused
    LaunchedEffect(conversation.title, isFocused) {
        if (!isFocused && titleField.text != conversation.title) {
            titleField = TextFieldValue(conversation.title)
        }
    }

    TextField(
        value = titleField,
        onValueChange = { value ->
            titleField = value
            onTitleChange(value.text)
        },
        singleLine = true,
        modifier = Modifier.onFocusChanged { focusState -> isFocused = focusState.isFocused },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}
