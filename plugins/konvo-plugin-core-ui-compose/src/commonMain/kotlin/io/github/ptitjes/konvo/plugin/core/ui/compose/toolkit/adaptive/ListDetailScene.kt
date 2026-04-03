package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import androidx.compose.runtime.*
import androidx.navigation3.runtime.*
import androidx.navigation3.scene.*
import androidx.window.core.layout.*

class ListDetailScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val listEntry: NavEntry<T>?,
    val detailEntry: NavEntry<T>,
) : Scene<T> {

    override val entries: List<NavEntry<T>> = buildList {
        listEntry?.let { add(it) }
        add(detailEntry)
    }

    override val content: @Composable (() -> Unit) = {
        CompositionLocalProvider(
            LocalListDetailPaneType provides ListDetailPaneType.TwoPane,
        ) {
            val detailMetadata = detailEntry.metadata[DETAIL_KEY] as? DetailMetadata
            val defaultListContent = detailMetadata?.defaultListContent

            ListDetailLayout(
                paneType = ListDetailPaneType.TwoPane,
                paneChoice = ListDetailPaneChoice.List,
                listContent =
                    defaultListContent?.let { content -> @Composable { content() } }
                        ?: listEntry?.let { entry -> @Composable { entry.Content() } }
                        ?: @Composable { },
                detailContent = {
                    detailEntry.Content()
                }
            )
        }
    }

    companion object {
        const val LIST_KEY = "ListDetailScene-List"
        const val DETAIL_KEY = "ListDetailScene-Detail"

        fun list(): Map<String, Any> = mapOf(LIST_KEY to true)
        fun detail(
            defaultListContent: (@Composable () -> Unit)? = null,
        ): Map<String, Any> = mapOf(DETAIL_KEY to DetailMetadata(defaultListContent))

        data class DetailMetadata(
            val defaultListContent: (@Composable () -> Unit)?,
        )
    }
}

class ListDetailStrategy<T : Any>(private val windowSizeClass: WindowSizeClass) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val isExpandedWidth = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
        if (!isExpandedWidth) return null

        val lastEntry = entries.lastOrNull() ?: return null
        val isListDetailEntry =
            lastEntry.metadata.containsKey(ListDetailScene.LIST_KEY) || lastEntry.metadata.containsKey(
                ListDetailScene.DETAIL_KEY
            )
        if (!isListDetailEntry) return null

        val listEntry = entries.findLast { ListDetailScene.LIST_KEY in it.metadata }
        val detailEntry = entries.findLast { ListDetailScene.DETAIL_KEY in it.metadata } ?: return null

        val previousEntries = entries.dropLast(1)

        return ListDetailScene(
            key = listEntry?.contentKey to detailEntry.contentKey,
            previousEntries = previousEntries,
            listEntry = listEntry,
            detailEntry = detailEntry,
        )
    }
}
