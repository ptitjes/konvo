package io.github.ptitjes.konvo.frontend.compose.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.settings.*
import io.github.ptitjes.konvo.frontend.compose.settings.defaultSettingsSections
import kotlinx.coroutines.flow.*

class SettingsListViewModel(

) : ViewModel() {

    private val _sections = MutableStateFlow(defaultSettingsSections)
    val sections: StateFlow<List<SettingsSection<*>>> = _sections.asStateFlow()

    private val _selectedSection = MutableStateFlow<SettingsSection<*>?>(null)
    val selectedSection: StateFlow<SettingsSection<*>?> = _selectedSection.asStateFlow()

    fun selectSection(section: SettingsSection<*>) {
        _selectedSection.value = section
    }

    fun unselectSection() {
        _selectedSection.value = null
    }
}

data class SettingsSection<T>(
    val key: SettingsSectionKey<T>,
    val title: String,
    val panel: SettingsSectionPanel<T>,
)

typealias SettingsSectionPanel<T> = @Composable (
    settings: T,
    updateSettings: ((T) -> T) -> Unit,
) -> Unit
