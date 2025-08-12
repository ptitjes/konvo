package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.*
import androidx.compose.material3.adaptive.navigationsuite.*
import androidx.compose.runtime.*
import androidx.window.core.layout.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import io.github.ptitjes.konvo.frontend.compose.screens.*
import io.github.ptitjes.konvo.frontend.compose.theme.*
import io.github.ptitjes.konvo.frontend.compose.util.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*

@Composable
fun App() {
    val adaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()

    KonvoTheme {
        NavigationSuiteScaffold(
            layoutType = suiteTypeFromAdaptiveInfo(adaptiveInfo),
            navigationSuiteItems = {
                item(
                    selected = true,
                    onClick = {},
                    icon = { Icon(imageVector = Icons.AutoMirrored.Default.Chat, contentDescription = null) },
                    badge = {},
                )
            },
        ) {
            val viewModel: ConversationListViewModel = viewModel()
            val selectedConversation by viewModel.selectedConversation.collectAsState()

            val paneType = drawerValueFromAdaptiveInfo(
                adaptiveInfo = adaptiveInfo,
                detailSelected = selectedConversation != null,
            )

            Surface(
                color = MaterialTheme.colorScheme.background,
            ) {
                ListDetailPane(
                    paneType = paneType,
                    list = {
                        ConversationListPanel()
                    },
                    detail = {
                        when (val conversation = selectedConversation) {
                            null -> {
                                NewConversationScreen(
                                    onConversationCreated = { viewModel.select(it) },
                                )
                            }

                            else -> {
                                ConversationScreen(
                                    conversation = conversation,
                                    onBackClick = { viewModel.select(null) },
                                )
                            }
                        }
                    },
                )
            }
        }
    }
}

private fun suiteTypeFromAdaptiveInfo(adaptiveInfo: WindowAdaptiveInfo): NavigationSuiteType {
    return with(adaptiveInfo) {
        if (windowPosture.isTabletop || windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT) {
            NavigationSuiteType.NavigationBar
        } else if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED ||
            windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM
        ) {
            NavigationSuiteType.NavigationRail
        } else {
            NavigationSuiteType.NavigationBar
        }
    }
}

private fun drawerValueFromAdaptiveInfo(
    adaptiveInfo: WindowAdaptiveInfo,
    detailSelected: Boolean,
): ListDetailPaneType {
    return with(adaptiveInfo) {
        when {
            windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED -> ListDetailPaneType.Both
            detailSelected -> ListDetailPaneType.Detail
            else -> ListDetailPaneType.List
        }
    }
}
