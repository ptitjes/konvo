package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*

/**
 * A component that displays a conversation with a text entry box.
 *
 * @param state The view state of the conversation to display
 * @param modifier The modifier to apply to this component
 */
@Composable
fun ConversationPane(
    state: ConversationViewState,
    modifier: Modifier = Modifier,
    onSendMessage: (String, List<Attachment>) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (val state = state) {
            is ConversationViewState.Loading -> Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
            }

            is ConversationViewState.Loaded -> Column(
                modifier = Modifier.widthIn(max = 800.dp),
            ) {
                val listState = rememberLazyListState()

                // Auto-scroll to the bottom when new entries are added
                LaunchedEffect(state.items.size, state.isProcessing) {
                    if (state.items.isNotEmpty() || state.isProcessing) {
                        val index = state.items.lastIndex + (if (state.isProcessing) 1 else 0)
                        listState.animateScrollToItem(index, scrollOffset = Int.MAX_VALUE)
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 32.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(state.items, key = { it.id }) { viewedItem ->
                        ConversationEventPanel(viewedItem)
                    }

                    if (state.isProcessing) {
                        item("__processing__") {
                            ConversationProcessingIndicator()
                        }
                    }
                }

                UserInputBox(
                    onSendMessage = onSendMessage,
                )
            }
        }
    }
}
