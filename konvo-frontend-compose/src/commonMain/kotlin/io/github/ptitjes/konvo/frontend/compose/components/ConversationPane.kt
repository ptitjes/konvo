package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*
import kotlinx.coroutines.*

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
    onUpdateLastReadMessageIndex: (Int) -> Unit,
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
                var firstComposition by remember { mutableStateOf(true) }

                val firstUnreadIndex = firstUnreadMessageIndex(state)

                // Bottom: last item, or processing indicator if active
                val lastListIndex = state.items.lastIndex + (if (state.isProcessing) 1 else 0)

                // Determine the initial first visible index: first unread if any, else bottom
                val initialFirstIndex =
                    (if (firstUnreadIndex != -1) firstUnreadIndex else lastListIndex)
                        .coerceAtLeast(0)
                val initialFirstScrollOffset =
                    if (firstUnreadIndex != -1) 0 else Int.MAX_VALUE

                val listState = rememberLazyListState(
                    initialFirstVisibleItemIndex = initialFirstIndex,
                    initialFirstVisibleItemScrollOffset = initialFirstScrollOffset,
                )

                // Auto-scroll to bottom only if all previous messages were read
                LaunchedEffect(state.items.size, state.isProcessing) {
                    if (!firstComposition) {
                        val hasItems = state.items.isNotEmpty()

                        val shouldScroll = when {
                            // New item appended: user must have read up to the previous last item
                            hasItems && !state.isProcessing -> state.lastReadMessageIndex >= state.items.lastIndex - 1
                            // Processing indicator visible: user must have read all items
                            state.isProcessing -> state.lastReadMessageIndex >= state.items.lastIndex
                            else -> false
                        }

                        if (shouldScroll) listState.animateScrollToItem(lastListIndex)
                    }
                }

                LaunchedEffect(Unit) { firstComposition = false }

                LastReadMessageIndexUpdater(
                    firstUnreadIndex = firstUnreadIndex,
                    state = state,
                    listState = listState,
                    onUpdateLastReadMessageIndex = onUpdateLastReadMessageIndex,
                )

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 32.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                ) {
                    itemsIndexed(state.items, key = { _, it -> it.id }) { index, viewedItem ->
                        Column {
                            if (index == firstUnreadIndex) NewMessagesDivider()
                            ConversationEventPanel(viewedItem)
                        }
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

@Composable
private fun firstUnreadMessageIndex(state: ConversationViewState.Loaded): Int =
    remember(state.items.size, state.lastReadMessageIndex, state.isProcessing) {
        val idx = state.lastReadMessageIndex + 1
        if (idx in 0..state.items.lastIndex) idx else -1
    }

@Composable
private fun LastReadMessageIndexUpdater(
    firstUnreadIndex: Int,
    state: ConversationViewState.Loaded,
    listState: LazyListState,
    onUpdateLastReadMessageIndex: (Int) -> Unit,
) {
    // When the user scrolls over new messages for > 5 seconds, update the last read index
    LaunchedEffect(firstUnreadIndex, state.items.size) {
        if (firstUnreadIndex == -1) return@LaunchedEffect
        var pendingJob: Job? = null
        snapshotFlow { listState.listVisibleItemIndex >= firstUnreadIndex }
            .collect { overNew ->
                if (overNew) {
                    if (pendingJob == null) {
                        pendingJob = launch {
                            delay(5_000)
                            // Re-check condition after delay using the last visible index
                            val lastVisibleNow = listState.listVisibleItemIndex
                            val stillOverNew = lastVisibleNow >= firstUnreadIndex
                            if (stillOverNew) {
                                val lastVisibleClamped = lastVisibleNow.coerceAtMost(state.items.lastIndex)
                                if (lastVisibleClamped > state.lastReadMessageIndex) {
                                    onUpdateLastReadMessageIndex(lastVisibleClamped)
                                }
                            }
                            pendingJob = null
                        }
                    }
                } else {
                    pendingJob?.cancel()
                    pendingJob = null
                }
            }
    }
}

private val LazyListState.listVisibleItemIndex: Int
    get() = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
