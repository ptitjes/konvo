package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import androidx.compose.runtime.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import com.slack.circuit.runtime.screen.Screen
import org.jetbrains.compose.resources.*

internal data class SettingsSectionView(val key: String) : Screen

interface SettingsSectionState : CircuitUiState

data class SettingsSection<S : SettingsSectionState>(
    val titleKey: String,
    val icon: DrawableResource,
    val title: @Composable () -> String,
    val presenterFactory: (navigator: SettingsNavigator) -> Presenter<S>,
    val panel: @Composable (S) -> Unit,
    val scrollable: Boolean = true,
    val children: List<SettingsSection<*>> = emptyList(),
)
