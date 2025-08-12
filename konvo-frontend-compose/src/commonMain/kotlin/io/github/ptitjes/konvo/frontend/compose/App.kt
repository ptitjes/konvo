package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.*
import androidx.compose.material3.adaptive.navigationsuite.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.window.core.layout.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import io.github.ptitjes.konvo.frontend.compose.theme.*
import io.github.ptitjes.konvo.frontend.compose.util.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*

@Composable
fun App() {
    val adaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()

    val appViewModel: AppViewModel = viewModel()

    KonvoTheme {
        NavigationSuiteScaffold(
            layoutType = suiteTypeFromAdaptiveInfo(adaptiveInfo),
            navigationSuiteItems = {
                AppState.entries.forEach { state ->
                    item(
                        selected = appViewModel.state == state,
                        onClick = { appViewModel.select(state) },
                        icon = {
                            Icon(
                                imageVector = state.icon,
                                contentDescription = state.contentDescription,
                            )
                        },
                        badge = {},
                    )
                }
            },
        ) {
            when (appViewModel.state) {
                AppState.Conversations -> ConversationsListDetailPane(
                    adaptiveInfo = adaptiveInfo,
                )

                AppState.Archive -> Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    text = "Archive",
                    style = MaterialTheme.typography.titleLarge,
                )

                AppState.KnowledgeBases -> Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    text = "Knowledge Bases",
                    style = MaterialTheme.typography.titleLarge,
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
