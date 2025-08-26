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
        ListDetailPaneScaffold(
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

private fun EntryProviderBuilder<Destination>.conversationEntries(navigator: Navigator) {
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

private fun EntryProviderBuilder<Destination>.archiveEntries(navigator: Navigator) {
    entry<Destination.Archive> {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            text = strings.navigationDestinationTitles(MainDestination.Archive),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

private fun EntryProviderBuilder<Destination>.knowledgeBaseEntries(navigator: Navigator) {
    entry<Destination.KnowledgeBase> {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            text = strings.navigationDestinationTitles(MainDestination.KnowledgeBases),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

private fun EntryProviderBuilder<Destination>.settingEntries(navigator: Navigator) {
    entry<Destination.Setting.List> {
        SettingsListScreen(
            navigator = navigator,
        )
    }
    entry<Destination.Setting.Section> {
        SettingsScreen(
            titleKey = it.key,
            navigator = navigator,
        )
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
