package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels.*

@Composable
fun ConversationsListDetailPane(
    adaptiveInfo: WindowAdaptiveInfo,
    viewModel: ConversationListViewModel = viewModel(),
) {
    val selectedConversation by viewModel.selectedConversation.collectAsState()
    val newConversation by viewModel.newConversation.collectAsState()

    ListDetailPane(
        adaptiveInfo = adaptiveInfo,
        paneChoice = when {
            selectedConversation != null || newConversation -> ListDetailPaneChoice.Detail
            else -> ListDetailPaneChoice.List
        },
        list = {
            ConversationListPanel {
                viewModel.createNewConversation()
            }
        },
        detail = {
            val conversation = selectedConversation
            when {
                conversation == null || newConversation -> {
                    NewConversationScreen(
                        onConversationCreated = { viewModel.select(it) },
                        onBackClick = { viewModel.cancelNewConversation() },
                    )
                }

                else -> {
                    ConversationScreen(
                        initialConversation = conversation,
                        onBackClick = { viewModel.select(null) },
                    )
                }
            }
        },
    )
}
