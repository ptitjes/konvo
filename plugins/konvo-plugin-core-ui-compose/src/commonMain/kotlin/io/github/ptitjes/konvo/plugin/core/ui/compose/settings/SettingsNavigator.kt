package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import com.slack.circuit.runtime.*

class SettingsNavigator(
    internal val navigator: Navigator,
) {
    internal val isLastSection: Boolean
        get() {
            val settingsScreens = navigator.peekBackStack().filterIsInstance<SettingsSectionScreen>()
            return settingsScreens.size == 1
        }

    internal fun closeSettings() {
        navigator.popUntil { it !is SettingsScreen }
    }

    fun goBack() {
        navigator.pop()
    }

    fun goToSection(key: String) {
        navigator.goTo(SettingsSectionScreen(key))
    }

    val selectedSectionKey: String?
        get() {
            val sectionScreen = navigator.peek() as? SettingsSectionScreen
            return sectionScreen?.key
        }
}
