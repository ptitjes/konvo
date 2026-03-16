package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.lifecycle.viewmodel.navigation3.*
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.*
import androidx.window.core.layout.*

@Composable
fun <T : Any> MainScreenScaffold(
    backStack: List<T>,
    onBack: () -> Unit,
    navigationPaneState: PaneState,
    extraPaneState: PaneState,
    entryProvider: (key: T) -> NavEntry<T>,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass

    val sceneStrategy = remember(windowSizeClass, navigationPaneState, extraPaneState) {
        CenterStageSceneStrategy<T>(
            windowSizeClass = windowSizeClass,
            navigationPaneState = navigationPaneState,
            extraPaneState = extraPaneState,
        )
    }

    Surface(modifier = modifier) {
        SharedTransitionLayout {
            NavDisplay(
                backStack = backStack,
                onBack = onBack,
                sceneStrategy = sceneStrategy,
                sharedTransitionScope = this,
                entryDecorators = listOf<NavEntryDecorator<T>>(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider,
                transitionSpec = {
                    fadeIn(animationSpec = tween(durationMillis = 250)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 250))
                }
            )
        }
    }
}

private fun paneTypeFromAdaptiveInfo(windowSizeClass: WindowSizeClass): ListDetailPaneType {
    val isExpandedWidth = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)
    return if (isExpandedWidth) ListDetailPaneType.TwoPane else ListDetailPaneType.OnePane
}
