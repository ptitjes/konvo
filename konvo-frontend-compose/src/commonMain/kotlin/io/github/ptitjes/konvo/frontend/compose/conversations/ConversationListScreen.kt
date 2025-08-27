package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.desktop.ui.tooling.preview.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.storage.inmemory.*
import io.github.ptitjes.konvo.frontend.compose.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.utils.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import kotlin.time.*

@Composable
fun ConversationListScreen(
    viewModel: ConversationListViewModel = viewModel(),
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val paneType = LocalListDetailPaneType.current
    LaunchedEffect(paneType) {
        if (paneType == ListDetailPaneType.TwoPane
            && navigator.backStack.last() == Destination.Conversation.List
        ) {
            navigator.backStack.add(Destination.Conversation.New)
        }
    }

    ConversationListScreen(
        modifier = modifier,
        viewModel = viewModel,
        selectedConversationId = navigator.selectedConversationId,
        onCreateConversation = { navigator.navigateToNewConversation() },
        onSelectConversation = { navigator.navigateToConversation(it) },
        onDeleteConversation = {
            if (navigator.selectedConversationId == it) {
                navigator.navigateToNewConversation()
            }
        }
    )
}

/**
 * Conversation list panel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    selectedConversationId: String?,
    onCreateConversation: () -> Unit,
    onSelectConversation: (id: String) -> Unit,
    onDeleteConversation: (id: String) -> Unit,
    viewModel: ConversationListViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val conversations by viewModel.conversations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(message = error!!)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.conversations.listTitle,
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onCreateConversation() },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = strings.conversations.newConversationAria,
                )
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> FullSizeProgressIndicator(modifier = Modifier.padding(paddingValues))
            conversations.isEmpty() -> EmptyConversationListPanel(
                modifier = Modifier.padding(paddingValues),
                onNewClick = onCreateConversation,
            )

            else -> {
                val listState = rememberLazyListState()

                LaunchedEffect(selectedConversationId) {
                    if (selectedConversationId != null) {
                        val index = conversations.indexOfFirst { it.id == selectedConversationId }
                        listState.reveal(index)
                    }
                }

                LazyColumn(
                    modifier = Modifier.padding(paddingValues).fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 64.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(conversations, key = { it.id }) { conversation ->
                        ConversationListItem(
                            conversation = conversation,
                            selected = conversation.id == selectedConversationId,
                            onClick = { onSelectConversation(conversation.id) },
                            onDelete = {
                                viewModel.delete(conversation)
                                onDeleteConversation(conversation.id)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ConversationListPanelPreview() {
    val repo = remember { InMemoryConversationRepository() }

    // Seed preview data
    LaunchedEffect(Unit) {
        repo.create(
            ConversationDigest(
                id = "1",
                title = "First",
                createdAt = Instant.fromEpochMilliseconds(0),
                updatedAt = Instant.fromEpochMilliseconds(0),
                participants = emptyList(),
                lastMessagePreview = "Hello world",
                messageCount = 1,
            )
        )
        repo.create(
            ConversationDigest(
                id = "2",
                title = "Second",
                createdAt = Instant.fromEpochMilliseconds(0),
                updatedAt = Instant.fromEpochMilliseconds(0),
                participants = emptyList(),
                lastMessagePreview = "Another message",
                messageCount = 3,
            )
        )
    }

    val vm = remember { ConversationListViewModel(repo) }

    ConversationListScreen(
        viewModel = vm,
        modifier = Modifier.fillMaxSize(),
        selectedConversationId = null,
        onCreateConversation = {},
        onSelectConversation = {},
        onDeleteConversation = {},
    )
}
