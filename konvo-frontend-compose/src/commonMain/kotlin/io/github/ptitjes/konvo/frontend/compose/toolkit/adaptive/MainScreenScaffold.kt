package io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive

import androidx.compose.animation.*
import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.navigation3.*
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.*
import androidx.window.core.layout.*

@Composable
fun <T : Any> MainScreenScaffold(
    backStack: List<T>,
    onBack: () -> Unit,
    entryProvider: (key: T) -> NavEntry<T>,
) {
    val adaptiveInfo = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)
    val windowSizeClass = adaptiveInfo.windowSizeClass
    val paneType = paneTypeFromAdaptiveInfo(adaptiveInfo)

    val sceneStrategy = remember(windowSizeClass) { ListDetailStrategy<T>(windowSizeClass) }

    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalListDetailPaneType provides paneType,
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = onBack,
                sceneStrategy = sceneStrategy,
                sharedTransitionScope = this@SharedTransitionLayout,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider,
            )
        }
    }
}

private fun paneTypeFromAdaptiveInfo(adaptiveInfo: WindowAdaptiveInfo): ListDetailPaneType = with(adaptiveInfo) {
    val isExpandedWidth = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)
    if (isExpandedWidth) ListDetailPaneType.TwoPane else ListDetailPaneType.OnePane
}
