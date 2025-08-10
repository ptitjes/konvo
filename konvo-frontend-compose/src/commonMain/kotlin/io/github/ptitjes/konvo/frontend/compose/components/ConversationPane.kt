package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*

/**
 * A component that displays a conversation with a text entry box.
 *
 * @param viewModel The view model of the conversation to display
 * @param modifier The modifier to apply to this component
 */
@Composable
fun ConversationPane(
    viewModel: ConversationViewModel,
    modifier: Modifier = Modifier,
) {
    val viewedItems by viewModel.viewItems.collectAsState()
    val isProcessing by viewModel.assistantIsProcessing.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        val listState = rememberLazyListState()

        // Auto-scroll to the bottom when new entries are added
        LaunchedEffect(viewedItems.size, isProcessing) {
            if (viewedItems.isNotEmpty()) {
                val index = viewedItems.lastIndex + (if (isProcessing) 1 else 0)
                listState.animateScrollToItem(index)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(viewedItems, key = { it.id }) { viewedItem ->
                ConversationEventPanel(viewedItem)
            }

            if (isProcessing) {
                item("__processing__") {
                    ConversationProcessingIndicator()
                }
            }
        }

        UserInputBox(
            onSendMessage = { content, attachments ->
                viewModel.sendUserMessage(content, attachments)
            }
        )
    }
}
