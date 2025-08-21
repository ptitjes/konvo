package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.frontend.compose.components.settings.*
import io.github.ptitjes.konvo.frontend.compose.util.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*

@Composable
fun SettingsListDetailPane(
    adaptiveInfo: WindowAdaptiveInfo,
    viewModel: SettingsListViewModel = viewModel(),
) {
    val sections by viewModel.sections.collectAsState()
    val selectedSection by viewModel.selectedSection.collectAsState()

    ListDetailPane(
        adaptiveInfo = adaptiveInfo,
        paneChoice = when {
            selectedSection != null -> ListDetailPaneChoice.Detail
            else -> ListDetailPaneChoice.List
        },
        list = {
            SettingsListPanel(
                sections = sections,
                selectedSection = selectedSection,
                onSelectSection = { viewModel.selectSection(it) }
            )
        },
        detail = {
            val section = selectedSection
            if (section != null) {
                key(section.title) {
                    SettingsScreen(
                        title = section.title,
                        onBackClick = { viewModel.unselectSection() },
                    ) {
                        when (section) {
                            is SettingsSection.WithoutKey -> SettingsPanelWithoutKey(section)
                            is SettingsSection.WithKey<*> -> SettingsPanelWithKey(section)
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun SettingsPanelWithoutKey(
    section: SettingsSection.WithoutKey,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
    ) {
        section.panel()
    }
}

@Composable
private fun <T> SettingsPanelWithKey(
    section: SettingsSection.WithKey<T>,
    viewModel: SettingsViewModel = viewModel(),
) {
    val settings by viewModel.getSettings(section.key).collectAsState()

    fun updateSettings(block: (T) -> T) {
        viewModel.updateSettings(section.key, block)
    }

    when (val settings = settings) {
        is SettingsViewState.Loading -> FullSizeProgressIndicator()
        is SettingsViewState.Loaded<T> -> Column(
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
        ) {
            section.panel(settings.value) { block ->
                updateSettings(block)
            }
        }
    }
}
