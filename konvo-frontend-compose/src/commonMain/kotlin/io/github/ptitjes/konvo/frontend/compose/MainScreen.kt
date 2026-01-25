package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.material3.*
import androidx.compose.material3.adaptive.*
import androidx.compose.material3.adaptive.navigationsuite.*
import androidx.compose.runtime.*
import androidx.navigation3.runtime.*
import androidx.window.core.layout.*
import io.github.ptitjes.konvo.frontend.compose.conversations.*
import io.github.ptitjes.konvo.frontend.compose.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels.*
import io.github.ptitjes.konvo.frontend.compose.translations.*

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel(),
) {
    val adaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()

    LaunchedEffect(viewModel.backStack) {
        println("Back stack changed: ${viewModel.backStack.joinToString(", ")}")
    }

    val navigator = viewModel.navigator

    NavigationSuiteScaffold(
        layoutType = suiteTypeFromAdaptiveInfo(adaptiveInfo),
        navigationSuiteItems = {
            MainDestination.entries.forEach { mainDestination ->
                item(
                    selected = navigator.isInMainDestination(mainDestination),
                    onClick = { navigator.navigateTo(mainDestination) },
                    icon = {
                        Icon(
                            imageVector = mainDestination.icon,
                            contentDescription = strings.navigationDestinationTitles(mainDestination),
                        )
                    },
                    badge = {},
                )
            }
        },
    ) {
        MainScreenScaffold(
            backStack = navigator.backStack,
            onBack = { navigator.navigateBack() },
            entryProvider = entryProvider {
                conversationEntries(navigator)
                archiveEntries(navigator)
                knowledgeBaseEntries(navigator)
                settingEntries(navigator)
            }
        )
    }
}

private fun EntryProviderScope<Destination>.conversationEntries(navigator: Navigator) {
    entry<Destination.Conversation.List> {
        ConversationListScreen(
            navigator = navigator,
        )
    }
    entry<Destination.Conversation.New> {
        NewConversationScreen(
            navigator = navigator,
        )
    }
    entry<Destination.Conversation.Selected> {
        ConversationScreen(
            conversationId = it.id,
            navigator = navigator,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun EntryProviderScope<Destination>.archiveEntries(navigator: Navigator) {
    entry<Destination.Archive> {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = strings.navigationDestinationTitles(MainDestination.Archive),
                        )
                    },
                )
            }
        ) { }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun EntryProviderScope<Destination>.knowledgeBaseEntries(navigator: Navigator) {
    entry<Destination.KnowledgeBase> {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = strings.navigationDestinationTitles(MainDestination.KnowledgeBases),
                        )
                    },
                )
            }
        ) { }
    }
}

private fun EntryProviderScope<Destination>.settingEntries(navigator: Navigator) {
    entry<Destination.Setting.List>(
//        metadata = SettingsDialogSceneStrategy.listPane(),
    ) {
        SettingsListScreen(
            navigator = navigator,
        )
    }
    entry<Destination.Setting.Section>(
//        metadata = SettingsDialogSceneStrategy.detailPane(),
    ) {
        SettingsScreen(
            titleKey = it.key,
            navigator = navigator,
        )
    }
}

private fun suiteTypeFromAdaptiveInfo(adaptiveInfo: WindowAdaptiveInfo): NavigationSuiteType = with(adaptiveInfo) {
    val isCompactHeight =
        !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
    val isAtLeastMediumWidth =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    return with(adaptiveInfo) {
        if (windowPosture.isTabletop || isCompactHeight) {
            NavigationSuiteType.NavigationBar
        } else if (isAtLeastMediumWidth) {
            NavigationSuiteType.NavigationRail
        } else {
            NavigationSuiteType.NavigationBar
        }
    }
}
