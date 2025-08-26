package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.runtime.*
import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.settings.*
import kotlinx.coroutines.flow.*

class SettingsListViewModel() : ViewModel() {

    private val _sections = MutableStateFlow(defaultSettingsSections)
    val sections: StateFlow<List<SettingsSection>> = _sections.asStateFlow()

    init {
        println("Initializing SettingsListViewModel")
    }

    override fun onCleared() {
        super.onCleared()
        println("Cleared SettingsListViewModel")
    }
}

sealed interface SettingsSection {
    val titleKey: String
    val scrollable: Boolean
    val children: List<SettingsSection>

    data class WithoutKey(
        override val titleKey: String,
        override val scrollable: Boolean = true,
        val panel: @Composable () -> Unit,
        override val children: List<SettingsSection>,
    ) : SettingsSection

    data class WithKey<T>(
        override val titleKey: String,
        override val scrollable: Boolean = true,
        val key: SettingsKey<T>,
        val panel: @Composable (settings: T, updateSettings: ((T) -> T) -> Unit) -> Unit,
        override val children: List<SettingsSection> = emptyList(),
    ) : SettingsSection
}

fun List<SettingsSection>.findSectionByTitleKey(titleKey: String): SettingsSection? {
    fun List<SettingsSection>.findSectionByTitleKey(): SettingsSection? {
        forEach { section ->
            if (section.titleKey == titleKey) return section
            section.children.findSectionByTitleKey()?.let { return it }
        }
        return null
    }

    return findSectionByTitleKey()
}
