package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import androidx.compose.runtime.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import org.jetbrains.compose.resources.*

interface SettingsSectionState : CircuitUiState

data class SettingsSection<S : SettingsSectionState>(
    val titleKey: String,
    val icon: DrawableResource,
    val title: @Composable () -> String,
    val presenterFactory: () -> Presenter<S>,
    val panel: @Composable SettingsPanelScope.(S) -> Unit,
    val scrollable: Boolean = true,
    val children: List<SettingsSection<*>> = emptyList(),
)
