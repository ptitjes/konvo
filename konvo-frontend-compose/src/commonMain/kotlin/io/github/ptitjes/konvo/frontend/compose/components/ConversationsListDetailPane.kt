package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.material3.*
import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import androidx.window.core.layout.*
import io.github.ptitjes.konvo.frontend.compose.ListDetailPane
import io.github.ptitjes.konvo.frontend.compose.ListDetailPaneType
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

    val paneType = drawerValueFromAdaptiveInfo(
        adaptiveInfo = adaptiveInfo,
        detailSelected = selectedConversation != null,
        newConversation = newConversation,
    )

    CompositionLocalProvider(LocalListDetailPaneType provides paneType) {
        Surface(
            color = MaterialTheme.colorScheme.background,
        ) {
            ListDetailPane(
                paneType = paneType,
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
    }
}

private fun drawerValueFromAdaptiveInfo(
    adaptiveInfo: WindowAdaptiveInfo,
    detailSelected: Boolean,
    newConversation: Boolean,
): ListDetailPaneType {
    return with(adaptiveInfo) {
        when {
            windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.Companion.EXPANDED -> ListDetailPaneType.Both
            detailSelected || newConversation -> ListDetailPaneType.Detail
            else -> ListDetailPaneType.List
        }
    }
}
