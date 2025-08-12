package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.desktop.ui.tooling.preview.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import io.github.ptitjes.konvo.core.conversation.storage.inmemory.InMemoryConversationRepository
import io.github.ptitjes.konvo.frontend.compose.util.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*
import kotlinx.coroutines.flow.*
import kotlin.time.*

/**
 * Responsive side panel that chooses NavigationRail for compact widths and PermanentNavigationDrawer for larger widths.
 * Place this at the top-level of your screen and provide the main [content] of the page.
 */
@Composable
fun TwoPaneConversationList(
    viewModel: ConversationListViewModel = viewModel(),
    modifier: Modifier = Modifier.Companion,
    content: @Composable () -> Unit,
) {
    PermanentNavigationDrawer(
        drawerContent = {
            ConversationListPanel(
                viewModel = viewModel,
                modifier = Modifier.width(320.dp),
            )
        },
        content = { content() },
        modifier = modifier,
    )
}

@Preview
@Composable
@OptIn(ExperimentalTime::class)
private fun TwoPaneConversationListPreview() {
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

    TwoPaneConversationList(viewModel = vm) {
        Box(Modifier.fillMaxSize())
    }
}
