package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.runtime.*
import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.settings.*
import kotlinx.coroutines.flow.*

class SettingsListViewModel() : ViewModel() {

    private val _sections = MutableStateFlow(defaultSettingsSections)
    val sections: StateFlow<List<SettingsSection>> = _sections.asStateFlow()

    private val _selectedSection = MutableStateFlow<SettingsSection?>(null)
    val selectedSection: StateFlow<SettingsSection?> = _selectedSection.asStateFlow()

    fun selectSection(section: SettingsSection) {
        _selectedSection.value = section
    }

    fun unselectSection() {
        _selectedSection.value = null
    }
}

sealed interface SettingsSection {
    val title: String
    val scrollable: Boolean
    val children: List<SettingsSection>

    data class WithoutKey(
        override val title: String,
        override val scrollable: Boolean = true,
        val panel: @Composable () -> Unit,
        override val children: List<SettingsSection>,
    ) : SettingsSection

    data class WithKey<T>(
        override val title: String,
        override val scrollable: Boolean = true,
        val key: SettingsKey<T>,
        val panel: @Composable (settings: T, updateSettings: ((T) -> T) -> Unit) -> Unit,
        override val children: List<SettingsSection> = emptyList(),
    ) : SettingsSection
}
