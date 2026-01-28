package io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive

import androidx.compose.runtime.*
import androidx.navigation3.runtime.*
import androidx.navigation3.scene.*
import androidx.window.core.layout.*

private class ListDetailScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val listEntry: NavEntry<T>,
    val detailEntry: NavEntry<T>?,
) : Scene<T> {
    override val entries: List<NavEntry<T>> = buildList {
        add(listEntry)
        detailEntry?.let { add(it) }
    }

    override val content: @Composable (() -> Unit) = {
        ListDetailLayout(
            paneType = ListDetailPaneType.TwoPane,
            paneChoice = ListDetailPaneChoice.List,
            listContent = {
                listEntry.Content()
            },
            detailContent = {
                detailEntry?.Content()
            }
        )
    }
}

class ListDetailStrategy<T : Any>(private val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val isExpandedWidth = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)
        if (!isExpandedWidth) return null

        val lastTwoEntries = entries.takeLast(2)

        val listEntry = lastTwoEntries.first()
        val detailEntry = lastTwoEntries.getOrNull(1)

        return ListDetailScene(
            key = listEntry.contentKey to detailEntry?.contentKey,
            previousEntries = entries.dropLast(1),
            listEntry = listEntry,
            detailEntry = detailEntry
        )
    }
}
