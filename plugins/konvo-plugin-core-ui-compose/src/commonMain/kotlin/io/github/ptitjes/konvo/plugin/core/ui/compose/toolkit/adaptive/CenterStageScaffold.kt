package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*

/**
 * A scaffold that layouts three main components: a navigation pane, a main content area, and an extra pane.
 *
 * The panes' expansion and collapse are animated smoothly. The behavior of each pane is configured
 * through [PaneProperties]:
 * - [PaneProperties.modal]: If true, the pane appears as an overlay with a scrim over the content.
 *   If false, it becomes part of the layout and pushes the content.
 * - [PaneProperties.hiddenWhenCollapsed]: If true, the pane fully hides itself when collapsed.
 *   If false, it collapses to [foldedWidth].
 *
 * @param navigationPaneState The state of the navigation pane.
 * @param navigationPaneProperties The properties of the navigation pane.
 * @param navigationPaneContent The content of the navigation pane.
 * @param extraPaneState The state of the extra pane.
 * @param extraPaneProperties The properties of the extra pane.
 * @param extraPaneContent The content of the extra pane.
 * @param modifier The modifier to be applied to the scaffold.
 * @param content The main content area.
 */
@Composable
internal fun CenterStageScaffold(
    navigationPaneState: PaneState = rememberPaneState(),
    navigationPaneProperties: PaneProperties = PaneProperties(),
    navigationPaneContent: (@Composable () -> Unit)? = null,
    extraPaneState: PaneState = rememberPaneState(),
    extraPaneProperties: PaneProperties = PaneProperties(),
    extraPaneContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier.Companion,
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    CenterStageScaffoldLayout(
        modifier = modifier,
        navigationExpanded = navigationPaneState.targetValue.isExpanded,
        extraExpanded = extraPaneState.targetValue.isExpanded,
        navigationProperties = navigationPaneProperties,
        onDismissNavigation = { coroutineScope.launch { navigationPaneState.collapse() } },
        navigationContent = navigationPaneContent,
        extraProperties = extraPaneProperties,
        onDismissExtra = { coroutineScope.launch { extraPaneState.collapse() } },
        extraContent = extraPaneContent,
        content = content,
    )
}

private val FoldedWidth = 72.dp
private val ExpandedWidth = 300.dp

data class PaneProperties(
    val modal: Boolean = false,
    val hiddenWhenCollapsed: Boolean = false,
    val foldedWidth: Dp = FoldedWidth,
    val expandedWidth: Dp = ExpandedWidth,
)

@Composable
private fun CenterStageScaffoldLayout(
    modifier: Modifier,
    navigationExpanded: Boolean,
    extraExpanded: Boolean,
    navigationProperties: PaneProperties = PaneProperties(),
    onDismissNavigation: () -> Unit = {},
    navigationContent: (@Composable (() -> Unit))? = null,
    extraProperties: PaneProperties = PaneProperties(),
    onDismissExtra: () -> Unit = {},
    extraContent: (@Composable (() -> Unit))? = null,
    content: @Composable (() -> Unit),
) {
    val navigationRatio by animateFloatAsState(if (navigationExpanded) 1f else 0f)
    val extraRatio by animateFloatAsState(if (extraExpanded) 1f else 0f)

    Layout(
        modifier = modifier.fillMaxSize(),
        content = {
            Box(Modifier.layoutId("content")) {
                content()
            }
            if (navigationContent != null) {
                Box(Modifier.layoutId("navigation")) {
                    navigationContent()
                }
            }
            if (extraContent != null) {
                Box(Modifier.layoutId("extra")) {
                    extraContent()
                }
            }
            if ((navigationProperties.modal && navigationContent != null) ||
                (extraProperties.modal && extraContent != null)
            ) {
                Box(Modifier.layoutId("scrim")) {
                    Scrim(
                        expanded = (navigationProperties.modal && navigationExpanded) ||
                                (extraProperties.modal && extraExpanded),
                        onClose = {
                            if (navigationProperties.modal && navigationExpanded) onDismissNavigation()
                            if (extraProperties.modal && extraExpanded) onDismissExtra()
                        },
                    )
                }
            }
        },
    ) { measurables, constraints ->

        fun calculatePaneWidth(properties: PaneProperties, ratio: Float): Int {
            val minWidth = if (properties.hiddenWhenCollapsed) 0 else properties.foldedWidth.roundToPx()
            return (minWidth + (properties.expandedWidth.roundToPx() - minWidth) * ratio).toInt()
        }

        val navigationCurrentWidth = calculatePaneWidth(navigationProperties, navigationRatio)
        val extraCurrentWidth = calculatePaneWidth(extraProperties, extraRatio)

        val navigationReservedWidth = when {
            navigationContent == null -> 0
            !navigationProperties.modal -> navigationCurrentWidth
            !navigationProperties.hiddenWhenCollapsed -> navigationProperties.foldedWidth.roundToPx()
            else -> 0
        }

        val extraReservedWidth = when {
            extraContent == null -> 0
            !extraProperties.modal -> extraCurrentWidth
            !extraProperties.hiddenWhenCollapsed -> extraProperties.foldedWidth.roundToPx()
            else -> 0
        }

        val contentWidth = (constraints.maxWidth - navigationReservedWidth - extraReservedWidth).coerceAtLeast(0)
        val contentPlaceable = measurables.first { it.layoutId == "content" }.measure(
            constraints.copy(minWidth = contentWidth, maxWidth = contentWidth),
        )

        val navigationPlaceable = measurables.find { it.layoutId == "navigation" }?.let { measurable ->
            val width = if (navigationProperties.hiddenWhenCollapsed) navigationProperties.expandedWidth.roundToPx()
            else navigationCurrentWidth

            measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
        }

        val extraPlaceable = measurables.find { it.layoutId == "extra" }?.let { measurable ->
            val width = if (extraProperties.hiddenWhenCollapsed) extraProperties.expandedWidth.roundToPx()
            else extraCurrentWidth

            measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
        }

        val scrimPlaceable = measurables.find { it.layoutId == "scrim" }?.measure(constraints)

        layout(constraints.maxWidth, constraints.maxHeight) {
            contentPlaceable.placeRelative(navigationReservedWidth, 0)

            scrimPlaceable?.placeRelative(0, 0)

            navigationPlaceable?.let {
                val x = if (navigationProperties.hiddenWhenCollapsed) {
                    -navigationProperties.expandedWidth.roundToPx() + navigationCurrentWidth
                } else {
                    0
                }
                it.placeRelative(x, 0)
            }

            extraPlaceable?.let {
                val x = constraints.maxWidth - extraCurrentWidth
                it.placeRelative(x, 0)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalTransitionApi::class)
private fun Scrim(
    expanded: Boolean,
    onClose: () -> Unit,
) {
    val scrimAlpha by animateFloatAsState(if (expanded) 1f else 0f)

    Scrim(
        open = expanded,
        onClose = { onClose() },
        alpha = { scrimAlpha },
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f),
    )
}

@Composable
private fun Scrim(open: Boolean, onClose: () -> Unit, alpha: () -> Float, color: Color) {
    val closeDrawer = "close"
    val dismissDrawer =
        if (open) {
            Modifier.pointerInput(onClose) { detectTapGestures { onClose() } }
                .semantics(mergeDescendants = true) {
                    contentDescription = closeDrawer
                    onClick {
                        onClose()
                        true
                    }
                }
        } else {
            Modifier.Companion
        }

    Canvas(Modifier.fillMaxSize().then(dismissDrawer)) { drawRect(color, alpha = alpha()) }
}
