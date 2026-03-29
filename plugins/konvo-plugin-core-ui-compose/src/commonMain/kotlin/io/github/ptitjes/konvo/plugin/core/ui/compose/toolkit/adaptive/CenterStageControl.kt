package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import androidx.compose.material3.*
import androidx.compose.runtime.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.*
import org.jetbrains.compose.resources.*

interface CenterStageControl {
    val navigationState: CenterStagePaneState
    fun onNavigationExpand(update: (Boolean) -> Boolean)
    val extraState: CenterStagePaneState
    fun onExtraExpand(update: (Boolean) -> Boolean)
}

enum class CenterStagePaneState {
    Hidden,
    Collapsed,
    Expanded,
}

val CenterStageControl.navigationExpanded: Boolean get() = navigationState == CenterStagePaneState.Expanded
val CenterStageControl.extraExpanded: Boolean get() = extraState == CenterStagePaneState.Expanded

fun CenterStageControl.toggleNavigation() = onNavigationExpand { !it }
fun CenterStageControl.toggleExtra() = onExtraExpand { !it }

@Composable
fun CenterStageControl.NavigationButton() {
    CenterStagePaneButton(
        buttonRole = when (navigationState) {
            CenterStagePaneState.Hidden -> CenterStageButtonRole.None
            CenterStagePaneState.Collapsed -> CenterStageButtonRole.PaneOpen
            CenterStagePaneState.Expanded -> CenterStageButtonRole.PaneClose
        },
        onButtonClick = this::toggleNavigation,
        paneOpenIcon = { Res.drawable.ic_left_panel_open },
        paneOpenDescription = { i18n.navigation.navigationOpenAria },
        paneCloseIcon = { Res.drawable.ic_left_panel_close },
        paneCloseDescription = { i18n.navigation.navigationCloseAria },
        fallback = {},
    )
}

@Composable
fun CenterStageControl.ContentNavigationButton(
    fallback: @Composable () -> Unit = {},
) {
    CenterStagePaneButton(
        buttonRole = when (navigationState) {
            CenterStagePaneState.Hidden -> CenterStageButtonRole.PaneOpen
            else -> CenterStageButtonRole.None
        },
        onButtonClick = this::toggleNavigation,
        paneOpenIcon = { Res.drawable.ic_left_panel_open },
        paneOpenDescription = { i18n.navigation.navigationOpenAria },
        paneCloseIcon = { Res.drawable.ic_left_panel_close },
        paneCloseDescription = { i18n.navigation.navigationCloseAria },
        fallback = fallback,
    )
}

@Composable
fun CenterStageControl.ContentExtraButton(
    fallback: @Composable (() -> Unit) = {},
) {
    CenterStagePaneButton(
        buttonRole = when (extraState) {
            CenterStagePaneState.Hidden -> CenterStageButtonRole.PaneOpen
            else -> CenterStageButtonRole.None
        },
        onButtonClick = this::toggleExtra,
        paneOpenIcon = { Res.drawable.ic_right_panel_open },
        paneOpenDescription = { i18n.navigation.detailsOpenAria },
        paneCloseIcon = { Res.drawable.ic_right_panel_close },
        paneCloseDescription = { i18n.navigation.detailsCloseAria },
        fallback = fallback,
    )
}

private enum class CenterStageButtonRole {
    None,
    Back,
    PaneOpen,
    PaneClose,
}

@Composable
private fun CenterStagePaneButton(
    buttonRole: CenterStageButtonRole,
    onButtonClick: () -> Unit,
    paneOpenIcon: () -> DrawableResource,
    paneOpenDescription: @Composable () -> String,
    paneCloseIcon: () -> DrawableResource,
    paneCloseDescription: @Composable () -> String,
    fallback: @Composable (() -> Unit),
) {
    if (buttonRole != CenterStageButtonRole.None) {
        IconButton(onClick = onButtonClick) {
            when (buttonRole) {
                CenterStageButtonRole.Back -> {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_back),
                        contentDescription = i18n.navigation.backAria
                    )
                }

                CenterStageButtonRole.PaneOpen -> {
                    Icon(
                        painterResource(paneOpenIcon()),
                        contentDescription = paneOpenDescription()
                    )
                }

                CenterStageButtonRole.PaneClose -> {
                    Icon(
                        painterResource(paneCloseIcon()),
                        contentDescription = paneCloseDescription()
                    )
                }
            }
        }
    } else {
        fallback()
    }
}
