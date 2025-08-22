package io.github.ptitjes.konvo.frontend.compose.toolkit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.*
import androidx.window.core.layout.*

enum class ListDetailPaneType {
    OnePane,
    TwoPane,
}

enum class ListDetailPaneChoice {
    List,
    Detail,
}

@Composable
fun ListDetailPane(
    adaptiveInfo: WindowAdaptiveInfo,
    paneType: ListDetailPaneType = paneTypeFromAdaptiveInfo(adaptiveInfo),
    paneChoice: ListDetailPaneChoice,
    list: @Composable () -> Unit,
    detail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    CompositionLocalProvider(LocalListDetailPaneType provides paneType) {
        Surface(modifier = modifier, color = containerColor, contentColor = contentColor) {
            ListDetailLayout(
                paneType = paneType,
                paneChoice = paneChoice,
                list = list,
                detail = detail,
            )
        }
    }
}

private fun paneTypeFromAdaptiveInfo(
    adaptiveInfo: WindowAdaptiveInfo,
): ListDetailPaneType {
    return with(adaptiveInfo) {
        when {
            windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED -> ListDetailPaneType.TwoPane
            else -> ListDetailPaneType.OnePane
        }
    }
}

@Composable
private fun ListDetailLayout(
    paneType: ListDetailPaneType,
    paneChoice: ListDetailPaneChoice,
    list: @Composable () -> Unit,
    detail: @Composable () -> Unit,
) {
    val maxListWidth = with(LocalDensity.current) { 300.dp.toPx().toInt() }

    Layout(
        content = {
            Box(Modifier.layoutId(ListTag)) {
                list()
            }
            Box(Modifier.layoutId(DetailTag)) {
                detail()
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
