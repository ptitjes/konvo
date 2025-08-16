package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import io.github.ptitjes.konvo.frontend.compose.screens.*
import io.github.ptitjes.konvo.frontend.compose.util.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*

@Composable
fun ConversationsListDetailPane(
    adaptiveInfo: WindowAdaptiveInfo,
    viewModel: ConversationListViewModel = viewModel(),
) {
    val selectedConversation by viewModel.selectedConversation.collectAsState()
    val newConversation by viewModel.newConversation.collectAsState()

    ListDetailPane(
        adaptiveInfo = adaptiveInfo,
        paneChoice = paneChoiceFromState(
            detailSelected = selectedConversation != null,
            newConversation = newConversation,
        ),
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

private fun paneChoiceFromState(detailSelected: Boolean, newConversation: Boolean): ListDetailPaneChoice {
    return when {
        detailSelected || newConversation -> ListDetailPaneChoice.Detail
        else -> ListDetailPaneChoice.List
    }
}
