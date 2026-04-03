package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.navigation3.runtime.*
import androidx.navigation3.scene.*
import androidx.window.core.layout.*
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_LARGE_LOWER_BOUND
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

        val navigationPaneProperties = remember(navigationMetadata?.properties, windowSizeClass, containerDpSize) {
            navigationMetadata?.properties ?: PaneProperties(
                modal = !widthAtLeastLarge,
                hiddenWhenCollapsed = !widthAtLeastExpanded,
                expandedWidth = when {
                    widthAtLeastExpanded -> containerDpSize.width * 1000 / 2618
                    else -> containerDpSize.width * .9f // TODO get default platform width
                },
            )
        }

        val extraPaneProperties = remember(extraMetadata?.properties, windowSizeClass, containerDpSize) {
            extraMetadata?.properties ?: PaneProperties(
                modal = !widthAtLeastLarge,
                hiddenWhenCollapsed = true,
                expandedWidth = when {
                    widthAtLeastExpanded -> containerDpSize.width * 1000 / 2618
                    else -> containerDpSize.width * .9f // TODO get default platform width
                },
            )
        }

        val defaultNavigationContent = contentMetadata?.defaultNavigationContent
        val defaultExtraContent = contentMetadata?.defaultExtraContent

        val coroutineScope = rememberCoroutineScope()

        val contentNavigationControl = remember(
            navigationPaneState, extraPaneState, navigationPaneProperties, extraPaneProperties,
            extraEntry, defaultExtraContent,
        ) {
            object : CenterStageControl {
                override val navigationState: CenterStagePaneState
                    get() = when {
                        navigationPaneState.targetValue.isExpanded -> CenterStagePaneState.Expanded
                        navigationPaneProperties.hiddenWhenCollapsed -> CenterStagePaneState.Hidden
                        else -> CenterStagePaneState.Collapsed
                    }

                override fun onNavigationExpand(update: (Boolean) -> Boolean) {
                    coroutineScope.launch { navigationPaneState.toggle() }
                }

                override val extraState: CenterStagePaneState
                    get() = when {
                        extraEntry == null && defaultExtraContent == null -> CenterStagePaneState.Hidden
                        extraPaneState.targetValue.isExpanded -> CenterStagePaneState.Expanded
                        extraPaneProperties.hiddenWhenCollapsed -> CenterStagePaneState.Hidden
                        else -> CenterStagePaneState.Collapsed
                    }

                override fun onExtraExpand(update: (Boolean) -> Boolean) {
                    coroutineScope.launch { extraPaneState.toggle() }
                }
            }
        }

        CompositionLocalProvider(
            LocalCenterStageControl provides contentNavigationControl
        ) {
            val navigationPaneContent =
                (defaultNavigationContent?.let { navigationContent -> @Composable { navigationContent() } }
                    ?: navigationEntry?.let { entry -> @Composable { entry.Content() } })

            val extraPaneContent = (extraEntry?.let { entry -> @Composable { entry.Content() } }
                ?: defaultExtraContent?.let { extraContent -> @Composable { extraContent() } })

            CenterStageScaffold(
                navigationPaneState = navigationPaneState,
                navigationPaneProperties = navigationPaneProperties,
                navigationPaneContent = navigationPaneContent,
                extraPaneState = extraPaneState,
                extraPaneProperties = extraPaneProperties,
                extraPaneContent = extraPaneContent,
                content = contentEntry::Content,
            )
        }
    }

    companion object {
        const val CONTENT_KEY = "CenterStageScene-Content"
        const val NAVIGATION_PANE_KEY = "CenterStageScene-NavigationPane"
        const val EXTRA_PANE_KEY = "CenterStageScene-ExtraPane"

        fun navigation(properties: PaneProperties? = null): Map<String, Any> =
            mapOf(NAVIGATION_PANE_KEY to PaneMetadata(properties))

        fun content(
            defaultNavigationContent: (@Composable () -> Unit)? = null,
            defaultExtraContent: (@Composable () -> Unit)? = null,
        ): Map<String, Any> =
            mapOf(CONTENT_KEY to ContentMetadata(defaultNavigationContent, defaultExtraContent))

        fun extra(properties: PaneProperties? = null): Map<String, Any> =
            mapOf(EXTRA_PANE_KEY to PaneMetadata(properties))
    }

    data class ContentMetadata(
        val defaultNavigationContent: (@Composable () -> Unit)?,
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
            lastEntry.metadata.containsKey(CenterStageScene.NAVIGATION_PANE_KEY) ||
                    lastEntry.metadata.containsKey(CenterStageScene.CONTENT_KEY) ||
                    lastEntry.metadata.containsKey(CenterStageScene.EXTRA_PANE_KEY)
        if (!isCenterStageEntry) return null

        val navigationEntry = entries.findLast { CenterStageScene.NAVIGATION_PANE_KEY in it.metadata }
        val contentEntry = entries.findLast { CenterStageScene.CONTENT_KEY in it.metadata } ?: return null
        val extraEntry = entries.findLast { CenterStageScene.EXTRA_PANE_KEY in it.metadata }

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
