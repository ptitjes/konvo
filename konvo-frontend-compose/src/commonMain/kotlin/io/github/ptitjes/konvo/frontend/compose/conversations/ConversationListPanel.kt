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
import io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import kotlin.time.*

/**
 * Conversation list panel.
 */
@Composable
fun ConversationListPanel(
    viewModel: ConversationListViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onCreateConversation: () -> Unit,
) {
    val conversations by viewModel.conversations.collectAsState()
    val selectedConversation by viewModel.selectedConversation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(message = error!!)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize().padding(horizontal = 8.dp),
    ) {
        when {
            isLoading -> FullSizeProgressIndicator()
            conversations.isEmpty() -> EmptyConversationListPanel(
                onNewClick = onCreateConversation,
            )

            else ->
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                            text = strings.conversations.listTitle,
                            style = MaterialTheme.typography.titleLarge,
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f),
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
                                    selected = conversation.id == selectedConversation?.id,
                                    onClick = { viewModel.select(conversation) },
                                    onDelete = { viewModel.delete(conversation) },
                                )
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = onCreateConversation,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = strings.conversations.newConversationAria,
                        )
                    }
                }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@Preview
@Composable
@OptIn(ExperimentalTime::class)
private fun ConversationListPanelPreview() {
    val repo = remember { InMemoryConversationRepository() }

    // Seed preview data
    LaunchedEffect(Unit) {
        repo.createConversation(
            Conversation(
                id = "1",
                title = "First",
                createdAt = Instant.fromEpochMilliseconds(0),
                updatedAt = Instant.fromEpochMilliseconds(0),
                participants = emptyList(),
                lastMessagePreview = "Hello world",
                messageCount = 1,
            )
        )
        repo.createConversation(
            Conversation(
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

    ConversationListPanel(
        viewModel = vm,
        modifier = Modifier.fillMaxSize(),
    ) { }
}
