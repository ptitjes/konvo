package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.*
import kotlinx.coroutines.*

object ConversationPane {
    object ItemPanels :
        ConversationView.Slot.Typed<LazyItemScope, ConversationViewState.Item, ConversationUserView>()
}

/**
 * A component that displays a conversation with a text entry box.
 *
 * @param state The view state of the conversation to display
 * @param modifier The modifier to apply to this component
 */
@Composable
context(_: ConversationUserView)
fun ConversationPane(
    state: ConversationViewState.Loaded,
    modifier: Modifier = Modifier,
    onUpdateLastReadMessageIndex: (Int) -> Unit,
    paddingValues: PaddingValues,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedVisibility(
            modifier = Modifier,
            visible = state.items.isEmpty(),
            enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            ConversationPreamble()
        }

        AnimatedVisibility(
            modifier = Modifier.weight(1f),
            visible = state.items.isNotEmpty(),
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            ConversationLog(
                state = state,
                onUpdateLastReadMessageIndex = onUpdateLastReadMessageIndex,
                paddingValues = paddingValues,
            )
        }

        ConversationSuggestions()

        ConversationInputBox()
    }
}

@Composable
context(_: ConversationUserView)
private fun ConversationPreamble() {
    val onBackground = MaterialTheme.colorScheme.onBackground

    BasicText(
        modifier = Modifier
            .padding(bottom = 32.dp)
            .widthIn(max = 800.dp)
            .padding(horizontal = 32.dp),
        text = "What can I do for you today?",
        autoSize = TextAutoSize.StepBased(maxFontSize = 42.sp),
        softWrap = false,
        color = { onBackground },
    )
}

@Composable
context(_: ConversationUserView)
private fun ConversationLog(
    state: ConversationViewState.Loaded,
    onUpdateLastReadMessageIndex: (Int) -> Unit,
    paddingValues: PaddingValues,
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

            val lastReadMessageIndex = state.digest.lastReadMessageIndex

            val shouldScroll = when {
                // New item appended: user must have read up to the previous last item
                hasItems && !state.isProcessing -> lastReadMessageIndex >= state.items.lastIndex - 1
                // Processing indicator visible: user must have read all items
                state.isProcessing -> lastReadMessageIndex >= state.items.lastIndex
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
        onUpdateLastReadMessageIndex = {
            onUpdateLastReadMessageIndex(it)
        },
    )

    val itemPanelView = LocalViewRegistry.current[ConversationPane.ItemPanels]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 0.dp) + paddingValues,
    ) {
        itemsIndexed(
            items = state.items,
            key = { _, item -> item.id },
            contentType = { _, item -> item::class },
        ) { index, viewedItem ->
            Column(modifier = Modifier.widthIn(max = 800.dp).padding(horizontal = 32.dp)) {
                if (index == firstUnreadIndex) NewMessagesDivider()

                with(itemPanelView) { Content(viewedItem) }
            }
        }

        conversationLogBottomItems(state)
    }
}

private fun LazyListScope.conversationLogBottomItems(state: ConversationViewState.Loaded) {
    // ConversationFeedBottom slot
    if (state.isProcessing) {
        item(ProcessingIndicatorKey) {
            Column(modifier = Modifier.widthIn(max = 800.dp).padding(horizontal = 32.dp)) {
                ConversationProcessingIndicator()
            }
        }
    }
}

@Composable
context(_: ConversationUserView)
private fun ConversationSuggestions() {
    // ConversationSuggestions slot
    // ConversationSuggestions()
}

@Composable
context(conversation: ConversationUserView)
private fun ConversationInputBox() {
    // UserInputBox slot (itself having sub slots)
    // - AttachmentButtonSlot slot
    //   - AttachmentMenu slot
    // - TextInput slot
    // - CommitButtonSlot slot

    val coroutineScope = rememberCoroutineScope()

    UserInputBox(
        modifier = Modifier.widthIn(max = 800.dp).padding(16.dp),
        onSendMessage = { content, attachments ->
            coroutineScope.launch {
                conversation.send(
                    payload = Messaging.Message(
                        content = listOf(Messaging.Part.Text(content)) + attachments.toMediaParts(),
                    ),
                )
            }
        },
    )
}

private object ProcessingIndicatorKey

@Composable
private fun firstUnreadMessageIndex(state: ConversationViewState.Loaded): Int =
    remember(state.items.size, state.digest.lastReadMessageIndex, state.isProcessing) {
        val idx = state.digest.lastReadMessageIndex + 1
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
                                if (lastVisibleClamped > state.digest.lastReadMessageIndex) {
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

private fun List<Messaging.Attachment>.toMediaParts(): List<Messaging.Part.Media> {
    return map { attachment ->
        when (attachment.type) {
            Messaging.Attachment.Type.Image -> Messaging.Part.Image(
                attachment.mimeType,
                attachment.name,
                attachment
            )

            Messaging.Attachment.Type.Video -> Messaging.Part.Video(
                attachment.mimeType,
                attachment.name,
                attachment
            )

            Messaging.Attachment.Type.Audio -> Messaging.Part.Audio(
                attachment.mimeType,
                attachment.name,
                attachment
            )

            Messaging.Attachment.Type.Document -> Messaging.Part.File(
                attachment.mimeType,
                attachment.name,
                attachment
            )
        }
    }
}
