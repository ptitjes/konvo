package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import androidx.compose.material3.*
import androidx.compose.runtime.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.*
import org.jetbrains.compose.resources.*

data class CenterStageControl(
    val navigationButtonRole: CenterStageButtonRole = CenterStageButtonRole.None,
    val onNavigationClick: () -> Unit = {},
    val extraButtonRole: CenterStageButtonRole = CenterStageButtonRole.None,
    val onExtraClick: () -> Unit = {},
)

enum class CenterStageButtonRole {
    None,
    Back,
    MenuOpen,
    MenuClose,
}

@Composable
fun CenterStageControl.NavigationButton(
    fallback: @Composable () -> Unit,
) {
    if (this.navigationButtonRole != CenterStageButtonRole.None) {
        IconButton(onClick = onNavigationClick) {
            when (this.navigationButtonRole) {
                CenterStageButtonRole.Back -> {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_back),
                        contentDescription = i18n.navigation.backAria
                    )
                }

                CenterStageButtonRole.MenuOpen -> {
                    Icon(
                        painterResource(Res.drawable.ic_left_panel_open),
                        contentDescription = i18n.navigation.navigationOpenAria
                    )
                }

                CenterStageButtonRole.MenuClose -> {
                    Icon(
                        painterResource(Res.drawable.ic_left_panel_close),
                        contentDescription = i18n.navigation.navigationCloseAria
                    )
                }
            }
        }
    } else {
        fallback()
    }
}

@Composable
fun CenterStageControl.ExtraPaneButton() {
    if (this.extraButtonRole != CenterStageButtonRole.None) {
        IconButton(onClick = onExtraClick) {
            when (this.extraButtonRole) {
                CenterStageButtonRole.Back -> {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_back),
                        contentDescription = i18n.navigation.backAria
                    )
                }

                CenterStageButtonRole.MenuOpen -> {
                    Icon(
                        painterResource(Res.drawable.ic_right_panel_open),
                        contentDescription = i18n.navigation.detailsOpenAria
                    )
                }

                CenterStageButtonRole.MenuClose -> {
                    Icon(
                        painterResource(Res.drawable.ic_right_panel_close),
                        contentDescription = i18n.navigation.detailsCloseAria
                    )
                }
            }
        }
    }
}
