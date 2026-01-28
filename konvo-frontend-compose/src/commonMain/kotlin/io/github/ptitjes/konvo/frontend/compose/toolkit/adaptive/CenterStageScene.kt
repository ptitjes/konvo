package io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive

import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.navigation3.runtime.*
import androidx.navigation3.scene.*
import androidx.window.core.layout.*
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
import io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive.CenterStageScene.Companion.CONTENT_KEY
import io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive.CenterStageScene.Companion.EXTRA_PANE_KEY
import io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive.CenterStageScene.Companion.NAVIGATION_PANE_KEY
import kotlinx.coroutines.*

class CenterStageScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    private val navigationEntry: NavEntry<T>?,
    private val contentEntry: NavEntry<T>,
    private val extraEntry: NavEntry<T>?,
    navigationPaneState: PaneState,
    extraPaneState: PaneState,
) : Scene<T> {
    override val entries: List<NavEntry<T>> = buildList {
        navigationEntry?.let { add(it) }
        add(contentEntry)
        extraEntry?.let { add(it) }
    }

    override val content: @Composable (() -> Unit) = {
        val windowSizeClass = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass
        val containerDpSize = LocalWindowInfo.current.containerDpSize

        val navigationMetadata = navigationEntry?.metadata?.get(NAVIGATION_PANE_KEY) as? PaneMetadata
        val extraMetadata = extraEntry?.metadata?.get(EXTRA_PANE_KEY) as? PaneMetadata
        val contentMetadata = contentEntry.metadata[CONTENT_KEY] as? ContentMetadata

        val widthAtLeastLarge = windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_LARGE_LOWER_BOUND)
        val widthAtLeastExpanded = windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

        val navigationPaneProperties = navigationMetadata?.properties
            ?: remember(windowSizeClass, containerDpSize) {
                PaneProperties(
                    modal = !widthAtLeastLarge,
                    hiddenWhenCollapsed = !widthAtLeastExpanded,
                    expandedWidth = when {
                        widthAtLeastExpanded -> containerDpSize.width * 1000 / 2618
                        else -> containerDpSize.width * .9f // TODO get default platform width
                    },
                )
            }

        val extraPaneProperties = extraMetadata?.properties
            ?: remember(windowSizeClass, containerDpSize) {
                PaneProperties(
                    modal = !widthAtLeastLarge,
                    hiddenWhenCollapsed = true,
                    expandedWidth = when {
                        widthAtLeastExpanded -> containerDpSize.width * 1000 / 2618
                        else -> containerDpSize.width * .9f // TODO get default platform width
                    },
                )
            }

        val defaultExtraContent = contentMetadata?.defaultExtraContent

        val coroutineScope = rememberCoroutineScope()

        val contentNavigationControl = CenterStageControl(
            navigationButtonRole =
                if (navigationEntry != null && navigationPaneProperties.hiddenWhenCollapsed) {
                    if (navigationPaneState.targetValue.isExpanded) CenterStageButtonRole.MenuClose
                    else CenterStageButtonRole.MenuOpen
                } else CenterStageButtonRole.None,
            onNavigationClick = { coroutineScope.launch { navigationPaneState.toggle() } },
            extraButtonRole =
                if ((extraEntry != null || defaultExtraContent != null) && extraPaneProperties.hiddenWhenCollapsed) {
                    if (extraPaneState.targetValue.isExpanded) CenterStageButtonRole.MenuClose
                    else CenterStageButtonRole.MenuOpen
                } else CenterStageButtonRole.None,
            onExtraClick = { coroutineScope.launch { extraPaneState.toggle() } },
        )

        CenterStageScaffold(
            navigationPaneState = navigationPaneState,
            navigationPaneProperties = navigationPaneProperties,
            navigationPaneContent = navigationEntry?.let { entry -> { entry.Content() } },
            extraPaneState = extraPaneState,
            extraPaneProperties = extraPaneProperties,
            extraPaneContent = extraEntry?.let { entry -> { entry.Content() } }
                ?: defaultExtraContent?.let { extraContent -> @Composable { extraContent() } },
            content = {
                CompositionLocalProvider(
                    LocalCenterStageControl provides contentNavigationControl
                ) {
                    contentEntry.Content()
                }
            },
        )
    }

    companion object {
        const val CONTENT_KEY = "CenterStageScene-Content"
        const val NAVIGATION_PANE_KEY = "CenterStageScene-NavigationPane"
        const val EXTRA_PANE_KEY = "CenterStageScene-ExtraPane"

        fun navigation(properties: PaneProperties? = null): Map<String, Any> =
            mapOf(NAVIGATION_PANE_KEY to PaneMetadata(properties))

        fun content(defaultExtraContent: (@Composable () -> Unit)? = null): Map<String, Any> =
            mapOf(CONTENT_KEY to ContentMetadata(defaultExtraContent))

        fun extra(properties: PaneProperties? = null): Map<String, Any> =
            mapOf(EXTRA_PANE_KEY to PaneMetadata(properties))
    }

    data class ContentMetadata(
        val defaultExtraContent: (@Composable () -> Unit)?,
    )

    data class PaneMetadata(
        val properties: PaneProperties?,
    )
}

class CenterStageSceneStrategy<T : Any>(
    private val windowSizeClass: WindowSizeClass,
    private val navigationPaneState: PaneState,
    private val extraPaneState: PaneState,
) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
//        val isExpandedWidth = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)
//        if (!isExpandedWidth) return null

        val lastEntry = entries.lastOrNull() ?: return null
        val isCenterStageEntry =
            lastEntry.metadata.containsKey(NAVIGATION_PANE_KEY) ||
                    lastEntry.metadata.containsKey(CONTENT_KEY) ||
                    lastEntry.metadata.containsKey(EXTRA_PANE_KEY)
        if (!isCenterStageEntry) return null

        val navigationEntry = entries.findLast { NAVIGATION_PANE_KEY in it.metadata }
        val contentEntry = entries.findLast { CONTENT_KEY in it.metadata } ?: return null
        val extraEntry = entries.findLast { EXTRA_PANE_KEY in it.metadata }

        return CenterStageScene(
            key = contentEntry.contentKey to (navigationEntry?.contentKey to extraEntry?.contentKey),
            previousEntries = entries.dropLast(1),
            navigationEntry = navigationEntry,
            contentEntry = contentEntry,
            extraEntry = extraEntry,
            navigationPaneState = navigationPaneState,
            extraPaneState = extraPaneState,
        )
    }
}
