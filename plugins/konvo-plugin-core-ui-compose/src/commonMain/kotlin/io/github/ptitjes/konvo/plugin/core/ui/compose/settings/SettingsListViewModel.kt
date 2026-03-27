package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import androidx.lifecycle.*
import io.github.ptitjes.syrup.*
import kotlinx.coroutines.flow.*

class SettingsListViewModel(
    pluginContext: PluginContext,
) : ViewModel() {

    private val settingsSections by pluginContext.contributions(SettingsSections)
    private val _sections = MutableStateFlow(settingsSections.toList())
    val sections: StateFlow<List<SettingsSection<*>>> = _sections.asStateFlow()

    init {
        println("Initializing SettingsListViewModel")

        // TODO Use DI to retrieve settings sections
        //
    }

    override fun onCleared() {
        super.onCleared()
        println("Cleared SettingsListViewModel")
    }
}

fun List<SettingsSection<*>>.findSectionByTitleKey(titleKey: String): SettingsSection<*>? {
    fun List<SettingsSection<*>>.findSectionByTitleKey(): SettingsSection<*>? {
        forEach { section ->
            if (section.titleKey == titleKey) return section
            section.children.findSectionByTitleKey()?.let { return it }
        }
        return null
    }

    return findSectionByTitleKey()
}
