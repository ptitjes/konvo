package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.*
import androidx.compose.material3.adaptive.navigationsuite.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.window.core.layout.*
import io.github.ptitjes.konvo.frontend.compose.conversations.*
import io.github.ptitjes.konvo.frontend.compose.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.images.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.theme.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels.*
import io.github.ptitjes.konvo.frontend.compose.translations.*

@Composable
fun App(
    appViewModel: AppViewModel = viewModel(),
) {
    val adaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()

    CoilImageLoader()

    val lyricist = rememberStrings()

    ProvideStrings(lyricist) {
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
                                    contentDescription = strings.navigationDestinationTitles(state),
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
                        text = strings.navigationDestinationTitles(AppState.Archive),
                        style = MaterialTheme.typography.titleLarge,
                    )

                    AppState.KnowledgeBases -> Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        text = strings.navigationDestinationTitles(AppState.KnowledgeBases),
                        style = MaterialTheme.typography.titleLarge,
                    )

                    AppState.Settings -> SettingsListDetailPane(
                        adaptiveInfo = adaptiveInfo,
                    )
                }
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
