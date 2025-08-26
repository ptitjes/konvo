package io.github.ptitjes.konvo.frontend.compose.toolkit.utils

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.lazy.*

/**
 * Reveals an item in a [androidx.compose.foundation.lazy.LazyList] at the specified index. If the item is not visible and above the visible items,
 * this method scrolls the list to reveal the item at the top of the viewport. If the item is not visible and below the
 * visible items, this method scrolls the list to reveal the item at the bottom of the viewport. If the item is already
 * visible, this method scrolls the list just enough to make it fully visible. This method takes the bottom and top
 * content padding of the list into account.
 */
suspend fun LazyListState.reveal(index: Int) {
    // Guard invalid indices
    val total = layoutInfo.totalItemsCount
    if (index !in 0..<total) return

    // If nothing is laid out yet, nothing to do
    if (layoutInfo.visibleItemsInfo.isEmpty()) return

    val beforePadding = layoutInfo.beforeContentPadding
    val afterPadding = layoutInfo.afterContentPadding
    val viewportStart = layoutInfo.viewportStartOffset
    val viewportEnd = layoutInfo.viewportEndOffset
    val availableTop = viewportStart + beforePadding
    val availableBottom = viewportEnd - afterPadding

    // Helper to refresh current item info after scrolling
    fun currentItemInfo() = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }

    val firstVisible = layoutInfo.visibleItemsInfo.first().index
    val lastVisible = layoutInfo.visibleItemsInfo.last().index
    val initiallyVisible = currentItemInfo()

    if (initiallyVisible == null) {
        // Item not visible
        if (index < firstVisible) {
            // Above: place it at the top (just after top padding)
            scrollToItem(index, beforePadding)
        } else if (index > lastVisible) {
            // Below: first bring it into view near the top, then adjust to bottom
            scrollToItem(index, beforePadding)

            // Recompute info and push it down to bottom if possible
            val info = currentItemInfo() ?: return
            val bottomEdge = info.offset + info.size
            val deltaToBottom = availableBottom - bottomEdge
            if (deltaToBottom > 0) {
                // Scroll down to align the item bottom with available bottom
                scrollBy(deltaToBottom.toFloat())
            }
        }
    } else {
        // Item is at least partially visible; ensure fully visible with minimal scrolling
        val topEdge = initiallyVisible.offset
        val bottomEdge = initiallyVisible.offset + initiallyVisible.size

        when {
            topEdge < availableTop -> {
                // Scroll down to reveal the top
                val delta = (topEdge - availableTop).toFloat()
                scrollBy(delta)
            }

            bottomEdge > availableBottom -> {
                // Scroll up to reveal the bottom
                val delta = (bottomEdge - availableBottom).toFloat()
                scrollBy(delta)
            }

            else -> {
                // Fully visible already; no-op
            }
        }
    }
}
