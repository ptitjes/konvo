package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.frontend.compose.util.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*

/**
 * Conversation list panel.
 */
@Composable
fun ConversationListPanel(
    viewModel: ConversationListViewModel = viewModel(),
    modifier: Modifier = Modifier,
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
            isLoading -> Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
            }

            conversations.isEmpty() -> EmptyConversationListPanel(
                onNewClick = {
                    /* Hooked up later in integration step */
                },
            )

            else ->
                Column(Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                        text = "Conversations",
                        style = MaterialTheme.typography.titleLarge,
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
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
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}
