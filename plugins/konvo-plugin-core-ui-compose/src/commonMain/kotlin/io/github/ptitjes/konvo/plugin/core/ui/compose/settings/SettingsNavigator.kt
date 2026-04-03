package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import com.slack.circuit.runtime.*

class SettingsNavigator(private val navigator: Navigator) {
    private val backStack get() = navigator.peekBackStack().filterIsInstance<SettingsScreen>().reversed()

    val selectedSectionKey: String? get() = backStack.lastOrNull()?.key

    fun goBack() {
        navigator.pop()
    }

    fun goToSection(key: String) {
        navigator.goTo(SettingsScreen(key))
    }
}
