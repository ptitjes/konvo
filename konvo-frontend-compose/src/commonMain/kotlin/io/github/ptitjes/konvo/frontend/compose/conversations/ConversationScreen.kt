package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.eygraber.compose.placeholder.*
import com.eygraber.compose.placeholder.material3.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*

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
    initialConversation: ConversationDigest,
    viewModel: ConversationViewModel = viewModel(initialConversation),
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val paneType = LocalListDetailPaneType.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    when (val state = state) {
                        is ConversationViewState.Loading -> Text(
                            text = "Conversation title",
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp)
                                .fillMaxWidth().placeholder(
                                    visible = true,
                                    highlight = PlaceholderHighlight.shimmer(),
                                ),
                        )

                        is ConversationViewState.Loaded -> EditableConversationTitle(
                            conversation = state.conversation,
                            onTitleChange = { viewModel.updateTitle(it) }
                        )
                    }
                },
                navigationIcon = {
                    if (paneType == ListDetailPaneType.OnePane) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = strings.conversations.backAria
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = strings.conversations.backAria
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = strings.conversations.settingsAria
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = state) {
            is ConversationViewState.Loading -> FullSizeProgressIndicator()
            is ConversationViewState.Loaded -> ConversationPane(
                state = state,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                onSendMessage = viewModel::sendUserMessage,
                onUpdateLastReadMessageIndex = viewModel::updateLastReadMessageIndex,
            )
        }
    }
}
