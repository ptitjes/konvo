package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import androidx.compose.runtime.*
import androidx.navigation3.runtime.*
import androidx.navigation3.scene.*
import androidx.window.core.layout.*

class ListDetailScene<T : Any>(
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
        CompositionLocalProvider(
            LocalListDetailPaneType provides ListDetailPaneType.TwoPane,
        ) {
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

    companion object {
        const val LIST_KEY = "ListDetailScene-List"
        const val DETAIL_KEY = "ListDetailScene-Detail"

        fun list(): Map<String, Any> = mapOf(LIST_KEY to true)
        fun detail(): Map<String, Any> = mapOf(DETAIL_KEY to true)
    }
}

class ListDetailStrategy<T : Any>(private val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val isExpandedWidth = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
        if (!isExpandedWidth) return null

        val lastEntry = entries.lastOrNull() ?: return null
        val isListDetailEntry = lastEntry.metadata.containsKey(ListDetailScene.Companion.LIST_KEY) || lastEntry.metadata.containsKey(
            ListDetailScene.Companion.DETAIL_KEY
        )
        if (!isListDetailEntry) return null

        val listEntry = entries.findLast { ListDetailScene.Companion.LIST_KEY in it.metadata } ?: return null
        val detailEntry = entries.findLast { ListDetailScene.Companion.DETAIL_KEY in it.metadata }

        val previousEntries = entries.dropLast(1)

        return ListDetailScene(
            key = listEntry.contentKey to detailEntry?.contentKey,
            previousEntries = previousEntries,
            listEntry = listEntry,
            detailEntry = detailEntry,
        )
    }
}
