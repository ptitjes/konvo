package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import androidx.compose.runtime.*
import org.jetbrains.compose.resources.*

data class SettingsSection(
    val titleKey: String,
    val icon: DrawableResource,
    val title: @Composable () -> String,
    val panel: @Composable SettingsPanelScope.() -> Unit,
    val scrollable: Boolean = true,
    val children: List<SettingsSection> = emptyList(),
)
