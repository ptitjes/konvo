package io.github.ptitjes.konvo.frontend.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.input.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import io.github.ptitjes.konvo.frontend.compose.util.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*

/**
 * A screen that displays a conversation with a top app bar.
 *
 * @param viewModel The view model of the conversation to display
 * @param onBackClick Callback for when the back button is clicked
 * @param modifier The modifier to apply to this component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    initialConversation: Conversation,
    viewModel: ConversationViewModel = viewModel(initialConversation),
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val conversation by viewModel.conversation.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
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
                            viewModel.updateTitle(value.text)
                        },
                        singleLine = true,
                        modifier = Modifier.onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        ConversationPane(
            state = state,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            onSendMessage = viewModel::sendUserMessage,
        )
    }
}
