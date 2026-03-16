package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.*

enum class ListDetailPaneType {
    OnePane,
    TwoPane,
}

enum class ListDetailPaneChoice {
    List,
    Detail,
}

@Composable
internal fun ListDetailLayout(
    paneType: ListDetailPaneType,
    paneChoice: ListDetailPaneChoice,
    listContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit,
) {
    val maxListWidth = with(LocalDensity.current) { 300.dp.toPx().toInt() }

    Layout(
        content = {
            Box(Modifier.layoutId(ListTag)) {
                listContent()
            }
            Box(Modifier.layoutId(DetailTag)) {
                detailContent()
            }
        }
    ) { measurables, constraints ->
        val listMeasurable = measurables.fastFirst { it.layoutId == ListTag }
        val detailMeasurable = measurables.fastFirst { it.layoutId == DetailTag }

        val layoutHeight = constraints.maxHeight
        val layoutWidth = constraints.maxWidth

        val listPlaceable = listMeasurable.measure(
            when {
                paneType == ListDetailPaneType.TwoPane -> {
                    val goldenSmallWidth = layoutWidth * 1000 / 2618
                    constraints.copy(
                        minWidth = goldenSmallWidth,
                        maxWidth = maxOf(maxListWidth, goldenSmallWidth),
                    )
                }

                paneChoice == ListDetailPaneChoice.List -> constraints
                paneChoice == ListDetailPaneChoice.Detail -> constraints.copy(maxWidth = 0)

                else -> error("Invalid pane type and choice: paneType=$paneType; paneChoice=$paneChoice")
            }
        )

        // Find the detail composable through it's layoutId tag
        val detailPlaceable = detailMeasurable.measure(
            constraints.copy(
                minWidth = layoutWidth - listPlaceable.width,
                maxWidth = layoutWidth - listPlaceable.width
            )
        )

        layout(layoutWidth, layoutHeight) {
            // Place the list component at the start of the screen.
            listPlaceable.placeRelative(0, 0)
            // Place detail to the side of the navigation component.
            detailPlaceable.placeRelative((listPlaceable.width), 0)
        }
    }
}

private const val ListTag = "list"
private const val DetailTag = "detail"
