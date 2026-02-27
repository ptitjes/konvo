package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.foundation.text.input.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds

@Composable
context(conversation: ConversationUserView)
fun EditableConversationTitle(
    conversationTitle: String?,
) {
    var isFocused by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val titleFieldState = rememberTextFieldState(conversationTitle ?: "")

    LaunchedEffect(titleFieldState) {
        @OptIn(FlowPreview::class)
        snapshotFlow { titleFieldState.text.toString() }.debounce(300.milliseconds).collectLatest {
            if ((it.isBlank() || it == conversationTitle) && !isFocused) return@collectLatest
            coroutineScope.launch { conversation.act(ConversationControl.TitleChange(it)) }
        }
    }

    TextField(
        state = titleFieldState,
        placeholder = { Text(text = LocalStrings.current.conversations.untitledConversationTitle) },
        lineLimits = TextFieldLineLimits.SingleLine,
        modifier = Modifier.onFocusChanged { focusState -> isFocused = focusState.isFocused },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}
