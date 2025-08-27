package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.*
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

data class SettingsSection(
    val titleKey: String,
    val panel: @Composable SettingsPanelScope.() -> Unit,
    val scrollable: Boolean = true,
    val children: List<SettingsSection> = emptyList(),
)

interface SettingsPanelScope {
    suspend fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration =
            if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
    ): SnackbarResult
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
