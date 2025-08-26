package io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive

import androidx.compose.runtime.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.*

class ListDetailStrategy<T : Any> : SceneStrategy<T> {
    @Composable
    override fun calculateScene(
        entries: List<NavEntry<T>>,
        onBack: (Int) -> Unit,
    ): Scene<T>? {
        val paneType = LocalListDetailPaneType.current
        if (paneType != ListDetailPaneType.TwoPane) return null

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
