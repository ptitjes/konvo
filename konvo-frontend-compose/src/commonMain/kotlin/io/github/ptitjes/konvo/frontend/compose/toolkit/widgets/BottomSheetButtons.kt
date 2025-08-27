package io.github.ptitjes.konvo.frontend.compose.toolkit.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.*

/**
 * Button that adapts to the pane type:
 * - OnePane: icon-only button
 * - TwoPane: uses a text button with optional icon and label
 */
@Composable
fun FilledActionButton(
    onClick: () -> Unit,
    enabled: Boolean,
    icon: @Composable (() -> Unit),
    label: @Composable (() -> Unit),
) {
    if (LocalListDetailPaneType.current == ListDetailPaneType.OnePane) {
        FilledIconButton(onClick = onClick, enabled = enabled) {
            icon.invoke()
        }
    } else {
        Button(onClick = onClick, enabled = enabled) {
            icon()
            Spacer(Modifier.width(8.dp))
            label()
        }
    }
}

/**
 * Button that adapts to the pane type:
 * - OnePane: icon-only button
 * - TwoPane: uses a text button with optional icon and label
 */
@Composable
fun OutlinedActionButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit),
    label: @Composable (() -> Unit),
) {
    if (LocalListDetailPaneType.current == ListDetailPaneType.OnePane) {
        OutlinedIconButton(onClick = onClick, enabled = enabled) {
            icon()
        }
    } else {
        OutlinedButton(onClick = onClick, enabled = enabled) {
            icon()
            Spacer(Modifier.width(8.dp))
            label()
        }
    }
}

/**
 * Button that adapts to the pane type:
 * - OnePane: icon-only button
 * - TwoPane: uses a text button with optional icon and label
 */
@Composable
fun FilledTonalActionButton(
    onClick: () -> Unit,
    enabled: Boolean,
    content: @Composable () -> Unit,
    label: (@Composable () -> Unit),
) {
    if (LocalListDetailPaneType.current == ListDetailPaneType.OnePane) {
        FilledTonalIconButton(onClick = onClick, enabled = enabled) {
            content()
        }
    } else {
        FilledTonalButton(onClick = onClick, enabled = enabled) {
            content()
            Spacer(Modifier.width(8.dp))
            label()
        }
    }
}
