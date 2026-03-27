package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import androidx.navigation3.runtime.*

class SettingsNavigator(
    val backStack: NavBackStack<SettingsScreen>,
) {
    val isLastSettingsSection: Boolean
        get() =
            backStack.size == 2

    fun closeSettings() {
        backStack.clear()
    }

    fun navigateBack() {
        backStack.removeLastOrNull()
    }

    fun navigateToSettingSection(titleKey: String) {
        backStack.add(SettingsSectionScreen(titleKey))
    }

    val selectedSettingSectionKey: String?
        get() = (backStack.lastOrNull() as? SettingsSectionScreen)?.key
}