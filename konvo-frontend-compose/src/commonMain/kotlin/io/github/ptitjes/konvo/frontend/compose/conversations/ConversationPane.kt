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
import kotlin.time.*

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
context(conversation: ConversationUserView)
private fun ConversationLog(
    state: ConversationViewState.Loaded,
    paddingValues: PaddingValues,
) {
    val lastViewedItemIndex = state.lastViewedItemIndex()

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = lastViewedItemIndex + 1)

    LastViewedTimestampUpdater(listState = listState, state = state)

    val itemPanelView = LocalViewRegistry.current[ConversationPane.ItemPanels]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = paddingValues,
    ) {
        itemsIndexed(
            items = state.items,
            key = { _, item -> item.id },
            contentType = { _, item -> item::class },
        ) { index, viewedItem ->
            Column(modifier = Modifier.widthIn(max = 800.dp).padding(vertical = 8.dp, horizontal = 32.dp)) {
                with(itemPanelView) { Content(viewedItem) }

                if (index == lastViewedItemIndex && index != state.items.lastIndex) NewMessagesDivider()
            }
        }

        conversationLogBottomItems(state)
    }
}

private fun LazyListScope.conversationLogBottomItems(state: ConversationViewState.Loaded) {
    // ConversationFeedBottom slot
    item(ProcessingIndicatorKey, contentType = ProcessingIndicatorKey) {
        if (state.isProcessing) {
            Column(modifier = Modifier.widthIn(max = 800.dp).padding(vertical = 8.dp, horizontal = 32.dp)) {
                ConversationProcessingIndicator()
            }
        }
    }
}

@Composable
context(conversation: ConversationUserView)
private fun ConversationViewState.Loaded.lastViewedItemIndex(): Int {
    return remember(presence, items) {
        val lastViewTimestamp = presence[conversation.participant]?.lastViewTimestamp ?: Instant.DISTANT_PAST
        val lastViewedItemIndex = items.indexOfFirst { it.timestamp > lastViewTimestamp }
            .takeIf { it != -1 } ?: items.lastIndex
        lastViewedItemIndex
    }
}

@Composable
context(conversation: ConversationUserView)
private fun LastViewedTimestampUpdater(
    listState: LazyListState,
    state: ConversationViewState.Loaded,
) {
    val lastMessageTimestamp = remember(state.preview) {
        state.preview.lastMessageTimestamp ?: Instant.DISTANT_PAST
    }
    val lastViewTimestamp = remember(state.presence) {
        state.presence[conversation.participant]?.lastViewTimestamp ?: Instant.DISTANT_PAST
    }

    val lastViewedItemIndex = state.lastViewedItemIndex()

    // When the user scrolls over new messages for > 5 seconds, update the last read index
    LaunchedEffect(lastViewedItemIndex, lastViewTimestamp, lastMessageTimestamp) {
        if (lastViewTimestamp >= lastMessageTimestamp) return@LaunchedEffect

        var pendingJob: Job? = null
        snapshotFlow {
            val lastVisibleIndex = listState.lastVisibleItemIndex.coerceAtMost(state.items.lastIndex)
            lastViewedItemIndex < lastVisibleIndex
        }
            .collect { overNew ->
                if (overNew) {
                    if (pendingJob == null) {
                        pendingJob = launch {
                            delay(5_000)

                            // Re-check condition after delay using the last visible index
                            val lastVisibleIndex = listState.lastVisibleItemIndex.coerceAtMost(state.items.lastIndex)
                            val lastVisibleItem = state.items[lastVisibleIndex]
                            val lastVisibleTimestamp = lastVisibleItem.timestamp

                            val stillOverNew = lastViewedItemIndex < lastVisibleIndex
                            if (stillOverNew) {
                                conversation.send(Presence.ViewNotification(lastVisibleTimestamp))
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

private val LazyListState.lastVisibleItemIndex: Int
    get() = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1

private object ProcessingIndicatorKey

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
                conversation.sendMessage(
                    listOf(Messaging.Part.Text(content)) + attachments.toMediaParts(),
                )
            }
        },
    )
}

private suspend fun ConversationUserView.sendMessage(content: List<Messaging.Part>) =
    send(Messaging.Message(content = content))

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
