package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*

@Composable
fun <T> SettingsPanelWithKey(
    section: SettingsSection.WithKey<T>,
    viewModel: SettingsViewModel = viewModel(),
) {
    val settings by viewModel.getSettings(section.key).collectAsState()

    fun updateSettings(block: (T) -> T) {
        viewModel.updateSettings(section.key, block)
    }

    when (val settings = settings) {
        is SettingsViewState.Loading -> FullSizeProgressIndicator()
        is SettingsViewState.Loaded<T> -> {
            val baseModifier = if (section.scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
            Column(modifier = baseModifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                section.panel(settings.value) { block ->
                    updateSettings(block)
                }
            }
        }
    }
}
